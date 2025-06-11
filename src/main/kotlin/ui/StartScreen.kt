package ui

import DebuggerConfig
import com.fazecast.jSerialComm.SerialPort
import connections.ProcessConnection
import connections.SerialConnection
import sourcemap.AsSourceMapping
import java.awt.Dimension
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

open class StartScreen(config: DebuggerConfig) : AboutScreen(config) {
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
    }

    override fun addOptions(mainPanel: JPanel) {
        val portComboBox = JComboBox<String>().apply {
            setAlignmentX(CENTER_ALIGNMENT)
            maximumSize = Dimension(250, -1)
            for (port in SerialPort.getCommPorts()) {
                addItem(port.systemPortPath)
            }
            selectedItem = config.port
        }
        mainPanel.add(portComboBox)
        val emulatorCheckbox = JCheckBox("Use emulator").apply {
            setAlignmentX(CENTER_ALIGNMENT)
            isSelected = config.useEmulator
        }
        mainPanel.add(emulatorCheckbox)
        mainPanel.add(JButton("Select program").apply {
            setAlignmentX(CENTER_ALIGNMENT)
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileSelectionMode = JFileChooser.FILES_ONLY
                chooser.fileFilter = FileNameExtensionFilter("WebAssembly binaries (.wasm)", "wasm")
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    isVisible = false
                    dispose()
                    startDebugger(chooser.selectedFile, emulatorCheckbox.isSelected, portComboBox.selectedItem as String)
                }
            }
        })
    }

    private fun startDebugger(binary: File, emulator: Boolean, comPort: String) {
        val connection = if (emulator) {
            ProcessConnection(config.wdcliPath, binary.path, "--no-socket")
        }
        else {
            SerialConnection(comPort)
        }
        val sourceMapping = AsSourceMapping(File(binary.path + ".map").readText())
        InteractiveDebugger(connection, config.symbolicWdcliPath, sourceMapping, binary.path, config = config)
    }
}
