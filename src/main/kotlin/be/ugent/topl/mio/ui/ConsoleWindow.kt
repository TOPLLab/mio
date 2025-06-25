package be.ugent.topl.mio.ui

import be.ugent.topl.mio.debugger.Debugger
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.text.DefaultCaret

class ConsoleWindow(debugger: Debugger) : JFrame("Console") {
    init {
        minimumSize = Dimension(300, 200)
        val textArea = JTextArea().apply {
            isEditable = false
        }
        val scrollPane = JScrollPane(textArea)
        val caret = textArea.caret as DefaultCaret
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)
        add(scrollPane)
        debugger.printListener = {
            textArea.text += it + "\n"
        }
        isAlwaysOnTop = true
    }
}
