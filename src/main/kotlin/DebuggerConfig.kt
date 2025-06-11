import java.io.FileInputStream
import java.util.*

class DebuggerConfig {
    private val properties = Properties()
    init {
        properties.load(FileInputStream("${System.getenv("HOME")}/.wardbg/debugger.properties"))
    }
    val warduinoDir: String = properties.getProperty("warduinoDir")
    val wdcliPath = properties.getProperty("wdcli")
    val symbolicWdcliPath = properties.getProperty("wdcli-symbolic", wdcliPath)
    val port = properties.getProperty("port")
    val fqbn = properties.getProperty("fqbn")
    val useEmulator = properties.getProperty("useEmulator", "false") == "true"
    val uiScale = properties.getProperty("uiScale", "1")
    val lightMode = properties.getProperty("lightMode", "true") == "true"
    val concolic = properties.getProperty("concolic", "false") == "true"
}
