import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import sourcemap.AsSourceMapping
import sourcemap.compileAndFlash
import sourcemap.compileWat
import sourcemap.getDwarfSourcemap
import connections.ProcessConnection
import connections.SerialConnection
import debugger.Debugger
import ui.InteractiveDebugger
import ui.StartScreen
import java.io.File
import java.io.FileNotFoundException
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun expectNArguments(args: Array<String>, n : Int) {
    if (args.size < n) {
        println("Expected at least $n argument(s) but found ${args.size}")
        exitProcess(1)
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        try {
            val config = DebuggerConfig()
            System.setProperty("apple.laf.useScreenMenuBar", "true")
            System.setProperty("apple.awt.application.name", "MIO")
            val startScreen = StartScreen(config)
            startScreen.isVisible = true
        } catch(_: FileNotFoundException) {
            JOptionPane.showMessageDialog(null, "Configuration file ~/.wardbg/debugger.properties not found!\nPlease read the \"Configuration\" section of the documentation.", "Invalid configuration", JOptionPane.ERROR_MESSAGE)
        }
        return
    }
    expectNArguments(args, 1)
    val config = DebuggerConfig()
    when (args[0]) {
        "debug" -> {
            expectNArguments(args, 2)
            val watFilename = args[1]
            val connection =
                if (config.useEmulator) {
                    if (watFilename.endsWith(".wat"))
                        ProcessConnection(config.wdcliPath, "${File(watFilename).nameWithoutExtension}.wasm", "--no-socket")
                    else {
                        expectNArguments(args, 3)
                        val wasmFilename = args[2]
                        ProcessConnection(config.wdcliPath, wasmFilename, "--no-socket")
                    }
                }
                else SerialConnection(config.port)
            val sourceMapping =
                if (watFilename.endsWith(".wasm.map"))
                    AsSourceMapping(File(watFilename).readText())
                else if (watFilename == "dwarf")
                    getDwarfSourcemap(args[2])
                else
                    compileWat(watFilename)
            System.setProperty("sun.java2d.uiScale", config.uiScale)
            val lightMode = config.lightMode
            if (lightMode)
                FlatIntelliJLaf.setup()
            else
                FlatDarkLaf.setup()
            if (args.size == 2)
                InteractiveDebugger(connection, config.symbolicWdcliPath, sourceMapping, lightMode = lightMode)
            else
                InteractiveDebugger(connection, config.symbolicWdcliPath, sourceMapping, args[2], lightMode = lightMode)
        }
        "repl" -> {
            val connection =
                if (config.useEmulator) {
                    expectNArguments(args, 2)
                    val wasmFilename = args[1]
                    ProcessConnection(config.wdcliPath, wasmFilename, "--no-socket")
                } else SerialConnection(config.port)
            val debugger = Debugger(connection)
            debugger.setSnapshotPolicy(Debugger.SnapshotPolicy.Checkpointing())
            debugger.repl()
            debugger.setSnapshotPolicy(Debugger.SnapshotPolicy.None())
            debugger.close()
        }
        "updateModule" -> {
            expectNArguments(args, 2)
            val wasmFilename = args[1]
            val connection =
                if (config.useEmulator) ProcessConnection(config.wdcliPath, wasmFilename, "--no-socket")
                else SerialConnection(config.port)
            val debugger = Debugger(connection)
            debugger.updateModule(args[1])
            debugger.close()
        }
        "run" -> {
            expectNArguments(args, 2)
            val wasmFilename = args[1]
            val connection =
                if (config.useEmulator) ProcessConnection(config.wdcliPath, wasmFilename, "--no-socket")
                else SerialConnection(config.port)
            val debugger = Debugger(connection)
            debugger.updateModule(args[1])
            debugger.run()
            debugger.repl()
            debugger.close()
        }
        "flash" -> {
            expectNArguments(args, 2)
            val watFilename = args[1]
            compileAndFlash(
                config.warduinoDir,
                watFilename,
                config.fqbn,
                config.port
            )
        }
        else -> {
            println("Invalid option \"${args[0]}\"!")
            exitProcess(1)
        }
    }
}
