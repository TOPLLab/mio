package be.ugent.topl.mio.ui

import be.ugent.topl.mio.debugger.Debugger
import be.ugent.topl.mio.woodstate.WOODDumpResponse
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import javax.script.ScriptEngineManager
import javax.swing.JPanel
import javax.swing.JSplitPane

class CustomView(debugger: Debugger) : AbstractView(debugger) {
    private val textArea = RSyntaxTextArea().apply {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
    }
    // TODO: Maybe we can use kts, kotlin script?
    private val scriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    init {
        add(JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            JPanel(),
            RTextScrollPane(textArea))
        )
        updateView(debugger.checkpoints.last()!!.snapshot)
    }

    override fun updateView(currentState: WOODDumpResponse) {
        val bindings = scriptEngine.createBindings()
        bindings.put("pc", currentState.pc)
        scriptEngine.eval(textArea.text, bindings)
    }
}
