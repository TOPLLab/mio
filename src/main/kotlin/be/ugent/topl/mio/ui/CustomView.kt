package be.ugent.topl.mio.ui

import be.ugent.topl.mio.debugger.Debugger
import be.ugent.topl.mio.woodstate.WOODDumpResponse
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.Color
import java.awt.Graphics
import java.sql.Blob
import javax.script.ScriptEngineManager
import javax.swing.JPanel
import javax.swing.JSplitPane

class CustomView(debugger: Debugger) : AbstractView(debugger) {
    private val textArea = RSyntaxTextArea().apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
    }
    private val panel = object : JPanel() {
        private val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")
        var script = ""
        var currentState: WOODDumpResponse? = null

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.color = Color.black

            scriptEngine.put("state", currentState)
            scriptEngine.put("g", g)
            scriptEngine.put("black", Color.black)
            scriptEngine.put("red", Color.red)
            scriptEngine.put("green", Color.green)
            scriptEngine.put("blue", Color.blue)
            scriptEngine.put("orange", Color.orange)
            scriptEngine.eval(script)
        }
    }
    // TODO: Maybe we can use kts, kotlin script?
    private val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    init {
        title = "Custom view"
        add(JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            panel,
            RTextScrollPane(textArea)).apply {
                resizeWeight = 0.5
            }
        )
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
        panel.script = textArea.text
        panel.repaint()
    }

    fun test(str: String) {
        println("Hello there!")
    }
}
