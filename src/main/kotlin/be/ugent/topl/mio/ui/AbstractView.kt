package be.ugent.topl.mio.ui

import be.ugent.topl.mio.debugger.Debugger
import be.ugent.topl.mio.woodstate.WOODDumpResponse
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

abstract class AbstractView(debugger: Debugger) : JFrame() {
    init {
        minimumSize = Dimension(200, 200)
        preferredSize = Dimension(400, 600)
        isVisible = true

        val listener = this::updateView
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                debugger.removeCurrentStateListener(listener)
            }
        })
        debugger.registerCurrentStateListener(listener)
    }

    abstract fun updateView(currentState: WOODDumpResponse)
}
