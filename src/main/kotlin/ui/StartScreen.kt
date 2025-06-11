package ui

import DebuggerConfig
import com.fazecast.jSerialComm.SerialPort
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.util.SystemInfo
import connections.ProcessConnection
import connections.SerialConnection
import sourcemap.AsSourceMapping
import java.awt.Dimension
import java.awt.Font
import java.awt.Image
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class StartScreen(val config: DebuggerConfig) : JFrame() {
    init {
        configureTheme()
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(400, 300)
        isResizable = false
        val mainPanel = JPanel()
        mainPanel.setLayout(BoxLayout(mainPanel, BoxLayout.Y_AXIS))
        mainPanel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
        mainPanel.add(Box.createVerticalGlue())
        mainPanel.add(JLabel(ImageIcon(ImageIcon(this.javaClass.getResource("/warduino-logo.png")).image.getScaledInstance(100, 100, Image.SCALE_SMOOTH))).apply {
            setAlignmentX(CENTER_ALIGNMENT)
        })
        mainPanel.add(JLabel("MIO Debugger").apply {
            setAlignmentX(CENTER_ALIGNMENT)
            font = font.deriveFont(Font.BOLD).deriveFont(30.0f)
        })
        mainPanel.add(JLabel("for WARDuino").apply {
            setAlignmentX(CENTER_ALIGNMENT)
            font = font.deriveFont(20.0f)
        })
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
        mainPanel.add(Box.createVerticalGlue())
        add(mainPanel)
    }

    private fun configureTheme() {
        if (SystemInfo.isMacFullWindowContentSupported) {
            this.rootPane.putClientProperty("apple.awt.transparentTitleBar", "true")
            this.rootPane.putClientProperty("apple.awt.fullWindowContent", "true")
        }
        if (config.lightMode) FlatIntelliJLaf.setup()
        else FlatDarkLaf.setup()
    }

    private fun startDebugger(binary: File, emulator: Boolean, comPort: String) {
        val connection = if (emulator) {
            ProcessConnection(config.wdcliPath, binary.path, "--no-socket")
        }
        else {
            SerialConnection(comPort)
        }
        val sourceMapping = AsSourceMapping(File(binary.path + ".map").readText())
        InteractiveDebugger(connection, config.symbolicWdcliPath, sourceMapping, binary.path, lightMode = config.lightMode)
    }
}
