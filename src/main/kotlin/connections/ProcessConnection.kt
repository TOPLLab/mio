package connections

class ProcessConnection(vararg command: String) : Connection {
    private val process = ProcessBuilder(*command).start()

    override fun bytesAvailable(): Int {
        return process.inputStream.available()
    }

    override fun read(buf: ByteArray): Int {
        if (!process.isAlive)
            throw RuntimeException("The process is no longer alive. Exit code ${process.exitValue()}")
        return process.inputStream.read(buf)
    }

    override fun write(buf: ByteArray) {
        process.outputStream.write(buf)
        process.outputStream.flush()
    }

    override fun close() {
        process.destroy()
    }
}
