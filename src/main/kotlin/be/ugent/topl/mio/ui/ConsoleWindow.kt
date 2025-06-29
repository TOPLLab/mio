package be.ugent.topl.mio.ui

import be.ugent.topl.mio.debugger.Debugger
import java.awt.Dimension
import java.awt.Font
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.text.DefaultCaret
import javax.swing.text.StyleContext
import java.io.File

class ConsoleWindow(debugger: Debugger) : JFrame("Console") {
    init {
        minimumSize = Dimension(300, 200)
        val textArea = JTextArea().apply {
            isEditable = false
        }
        val iStream = javaClass.getResourceAsStream("/fonts/jetbrains/JetBrainsMono-Regular.ttf")
        val customFont = Font.createFont(Font.TRUETYPE_FONT, iStream)
        textArea.font = customFont.deriveFont(Font.PLAIN, 13.0f)
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
