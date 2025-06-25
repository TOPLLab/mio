package be.ugent.topl.mio

import java.io.FileInputStream
import java.util.*

class DebuggerConfig {
    private val properties = Properties()
    val configDir = "${System.getenv("HOME")}/.wardbg"
    private val propertiesLocation = "$configDir/debugger.properties"
    init {
        properties.load(FileInputStream(propertiesLocation))
    }
    val warduinoDir: String = properties.getProperty("warduinoDir")
    val wdcliPath = properties.getProperty("wdcli")
    val symbolicWdcliPath = properties.getProperty("wdcli-symbolic", wdcliPath)
    val port = properties.getProperty("port")
    val fqbn = properties.getProperty("fqbn")
    val useEmulator = properties.getProperty("useEmulator", "false") == "true"
    val uiScale = properties.getProperty("uiScale", "1")
    val lightMode = properties.getProperty("lightMode", "true") == "true"
    val macIntegratedToolbar = properties.getProperty("mac.integratedToolbar", "false") == "true"
    val concolic = properties.getProperty("concolic", "false") == "true"
}
