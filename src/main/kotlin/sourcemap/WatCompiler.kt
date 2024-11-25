package sourcemap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

data class WatSourceMapping(val sourceFile: String, private val pcLineMap: Map<Int, Int>, private val linePcMap: Map<Int, Int>): SourceMap {
    override fun getLineForPc(pc: Int): Int {
        return pcLineMap[pc] ?: throw IllegalArgumentException("The given pc (pc = ${pc.toString(16)}) does not match any instruction!")
    }

    override fun getPcForLine(line: Int, filename: String): Int {
        return linePcMap[line] ?: throw IllegalArgumentException("The given line $line does not match any instruction.")
    }

    override fun getSourceFile(pc: Int): String {
        return File(sourceFile).readText()
    }

    override fun getSourceFileName(pc: Int): String = sourceFile
}

fun compileWat(filename: String, compiler: String = "wat2wasm"): SourceMap {
    val process = ProcessBuilder(
        compiler,
        "--no-canonicalize-leb128s",
        "--disable-bulk-memory",
        "--debug-names",
        "-v",
        filename
    ).redirectErrorStream(true).start()
    val lineScanner = Scanner(process.inputStream)
    val objectMapper = ObjectMapper()
    objectMapper.registerKotlinModule()
    var sourcemapInfo: SourceMapLine? = null
    val pcLineMap = mutableMapOf<Int, Int>()
    val linePcMap = mutableMapOf<Int, Int>()
    while (lineScanner.hasNextLine()) {
        val line = lineScanner.nextLine()
        println(line)
        if (line.startsWith("@ {")) {
            sourcemapInfo = objectMapper.readValue(line.substring(2), SourceMapLine::class.java)
            continue
        }

        if (sourcemapInfo == null)
            continue

        val pc = line.substring(0, line.indexOf(':')).toInt(16)
        pcLineMap[pc] = sourcemapInfo.line
        linePcMap[sourcemapInfo.line] = pc
        sourcemapInfo = null
    }
    if (process.waitFor() != 0) {
        throw Exception("Failed to compile \"$filename\"!")
    }
    return WatSourceMapping(filename, pcLineMap, linePcMap)
}

fun flash(warduinoDir: String, fqbn: String, port: String): Boolean {
    val platformPath = "$warduinoDir/platforms/Arduino/"
    val builder = runInShell("make flash")
    builder.directory(File(platformPath))
    builder.environment()["FQBN"] = fqbn
    builder.environment()["PORT"] = port
    val configFilePath = Paths.get("$platformPath.config")
    val configFilePresent = Files.exists(configFilePath)
    if (configFilePresent) {
        Files.move(configFilePath, Paths.get("$platformPath.config.disabled"))
    }
    val proc = builder.inheritIO().start()
    val exitCode = proc.waitFor()
    if (configFilePresent) {
        Files.move(Paths.get("$platformPath.config.disabled"), configFilePath)
    }
    return exitCode == 0
}

fun recompile(warduinoDir: String, wasmFile: File, fqbn: String): Boolean {
    val platformPath = "$warduinoDir/platforms/Arduino/"
    val builder = runInShell("make recompile")
    builder.directory(File(platformPath))
    builder.environment()["FQBN"] = fqbn
    builder.environment()["BINARY"] = wasmFile.absolutePath
    val configFilePath = Paths.get("$platformPath.config")
    val configFilePresent = Files.exists(configFilePath)
    if (configFilePresent) {
        Files.move(configFilePath, Paths.get("$platformPath.config.disabled"))
    }
    val proc = builder.inheritIO().start()
    val exitCode = proc.waitFor()
    if (configFilePresent) {
        Files.move(Paths.get("$platformPath.config.disabled"), configFilePath)
    }
    return exitCode == 0
}

fun compileAndFlash(warduinoDir: String, watFilename: String, fqbn: String, port: String, watCompiler: String = "wat2wasm"):  SourceMap {
    val watFile = File(watFilename)
    val sourceMapping = compileWat(
        watFile.absolutePath,
        watCompiler
    )
    val wasmFilename = watFile.nameWithoutExtension + ".wasm"
    if (!recompile(warduinoDir, File(wasmFilename), fqbn)) {
        throw Exception("Recompile failed, wasm file = \"${wasmFilename}\".")
    }
    if (!flash(warduinoDir, fqbn, port)) {
        throw Exception("Failed to flash WARDuino!")
    }
    return sourceMapping
}

fun runInShell(command: String): ProcessBuilder {
    val isWindows = System.getProperty("os.name").lowercase(Locale.getDefault()).startsWith("windows")
    val builder = ProcessBuilder()
    if (isWindows) {
        builder.command("cmd.exe", "/c", command)
    } else {
        builder.command("/bin/bash", "-c", command)
    }
    return builder
}
