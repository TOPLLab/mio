package be.ugent.topl.mio.ui

import be.ugent.topl.mio.debugger.Debugger
import be.ugent.topl.mio.woodstate.WOODDumpResponse
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import javax.script.ScriptEngineManager
import javax.swing.*

class CustomView(debugger: Debugger) : AbstractView(debugger) {
    private val textArea = RSyntaxTextArea().apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
        //syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_KOTLIN
    }
    private val panel = object : JPanel() {
        private val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")
        //private val scriptEngine = ScriptEngineManager().getEngineByName("kotlin")
        //private val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
        var currentState: WOODDumpResponse? = null

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.color = Color.black

            scriptEngine.put("state", currentState)
            scriptEngine.put("g", g)
            scriptEngine.put("black", Color.black)
            scriptEngine.put("red", Color.red)
            scriptEngine.put("green", Color.green)
            scriptEngine.put("blue", Color.blue)
            scriptEngine.put("orange", Color.orange)
            val startTime = System.currentTimeMillis()
            scriptEngine.eval(getScript())
            println("Elapsed ${System.currentTimeMillis() - startTime}")
        }

        fun getScript(): String {
            return textArea.text
            //return "import java.awt.Color\nval g2 = g as java.awt.Graphics2D\n" + textArea.text // TODO: When catching exceptions we should shift the line number with 1
        }
    }
    // TODO: Maybe we can use kts, kotlin script?
    private val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    init {
        title = "Custom view"
        val scrollPane = RTextScrollPane(textArea)
        val splitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            panel,
            scrollPane
        ).apply {
            resizeWeight = 0.5
        }
        val box = JPanel()
        box.layout = BorderLayout()
        //val topBar = Box.createHorizontalBox()
        val topBar = JToolBar()
        topBar.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        //topBar.add(JLabel("View name"))
        topBar.add(Box.createHorizontalGlue())
        val reloadButton = JButton("Reload").apply {
            addActionListener {
                panel.repaint()
            }
        }
        topBar.add(reloadButton)
        topBar.add(JButton("View").apply {
            var lastPos = 0
            addActionListener {
                text = if (scrollPane.isVisible) {
                    lastPos = splitPane.dividerLocation
                    "Edit"
                }
                else {
                    splitPane.dividerLocation = lastPos
                    "View"
                }
                scrollPane.isVisible = !scrollPane.isVisible
                reloadButton.isVisible = scrollPane.isVisible
                revalidate()
                println(splitPane.dividerLocation)
            }
        })
        box.add(topBar, BorderLayout.NORTH)
        box.add(splitPane, BorderLayout.CENTER)
        add(box)
        //language=JavaScript
        textArea.text = """
            g.color = black
            g.drawString("Pc = " + state.pc, 0, 14);
            
            //for each (var el in state.io) {
            for (var i = 0; i < state.io.length; i++) {
                var ioElement = state.io[i];
                if (ioElement.value == 1) g.color = red
                else g.color = black
                g.drawString("io[" + i + "]: key = " + ioElement.key + ", value = " + ioElement.value, 0, 28 + i * 14 | 0);
            }
            
            // Checkerboard pattern
            for (var y = 0; y < 8; y++) {
                for (var x = 0; x < 8; x++) {
                    if ((x + y) % 2 == 0) g.color = black
                    else g.color = red
                    g.fillRect(300 + x * 50, y * 50, 50, 50)
                }
            }
            
            /*for (var i = 0; i < state.io.length; i++) {
            var ioElement = state.io[i];
            if (ioElement.key == "p17") {
                g.color = black
                if(ioElement.value == 1) {
                    g.color = green	
                }
                //g.fillRoundRect(5 + 105 * 2 | 0, 5, 100, 100, 10, 10)
                g.fillOval(5 + 105 * 2 | 0, 5, 100, 100)
            }
            if (ioElement.key == "p16") {
                g.color = black
                if(ioElement.value == 1) {
                    g.color = orange	
                }
                //g.fillRoundRect(5 + 105 * 1 | 0, 5, 100, 100, 10, 10)
                g.fillOval(5 + 105 * 1 | 0, 5, 100, 100)
            }
            if (ioElement.key == "p15") {
                g.color = black
                if(ioElement.value == 1) {
                    g.color = red	
                }
                //g.fillRoundRect(5, 5, 100, 100, 10, 10)
                g.fillOval(5, 5, 100, 100)
            }
        }*/
        """.trimIndent()
        //language=kotlin
        textArea.text = $$"""
            g2.color = Color.black
            g2.drawString("pc = 0x${state.pc?.toString(16)}", 0, 90)
            
            val colorMap = mutableMapOf<String,Color>(
                "p15" to Color.red,
                "p16" to Color.orange,
                "p17" to Color.green
            )
            
            for (el in state.io!!) {
                if (el.key.startsWith("p")) {
                    g2.color = if (el.value == 1) colorMap.getOrDefault(el.key, Color.red) 
                                else Color.black
                    
                    val index = Integer.parseInt(el.key.substring(1))
                    g2.fillOval(5 + (index % 15) * 35, 5 + (index / 15) * 35, 30, 30)
                }
            }
        """.trimIndent()
        textArea.text = """
            var colorMap = {
                "p15": red,
                "p16": orange,
                "p17": green
            }

            for each (el in state.io) {
                if (el.key.startsWith("p")) {
                    g.color = el.value === 1 ? colorMap[el.key] || red : black
                    
                    var index = parseInt(el.key.substring(1))
                    g.fillOval(5 + (index % 15) * 35, 5 + Math.floor(index / 15) * 35, 30, 30)
                }
            }
        """.trimIndent()
        textArea.tabSize = 4
        textArea.tabsEmulated = true
        updateView(debugger.checkpoints.last()!!.snapshot)
    }

    override fun updateView(currentState: WOODDumpResponse) {
        /*val bindings = scriptEngine.createBindings()
        bindings.put("pc", currentState.pc)

        bindings.put("view", this);
        bindings.put("panel", panel);
        scriptEngine.eval(textArea.text, bindings)*/

        panel.currentState = currentState
        //panel.script = "import java.awt.Color\nval g2 = g as java.awt.Graphics2D\n" + textArea.text // TODO: When catching exceptions we should shift the line number with 1
        panel.repaint()
    }

    fun test(str: String) {
        println("Hello there!")
    }
}
