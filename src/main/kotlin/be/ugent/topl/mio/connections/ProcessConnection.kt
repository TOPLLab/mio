package be.ugent.topl.mio.connections

import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread

class ProcessConnection(vararg command: String, private val name: String ="ProcessConnection") : Connection {
    private val process = ProcessBuilder(*command).start()
    private val buffers = ConcurrentLinkedDeque<ByteArray>()
    /*private val buffer = ByteArray(4096)
    private var bytesAvailable = 0*/
    init {
        thread {
            while (process.isAlive) {
                val available = process.inputStream.available()
                if (available > 0) {
                    val buffer = ByteArray(available)
                    process.inputStream.read(buffer)
                    buffers.add(buffer)
                    //print(String(buffer))
                    // TODO: Wait if there is no space but maybe give a warning because we don't want this.
                    /*buffer.copyInto(buffer, bytesAvailable)
                    bytesAvailable += available*/
                } /*else {
                    Thread.sleep(10)
                }*/
            }
        }
    }

    override fun bytesAvailable(): Int {
        //return process.inputStream.available()
        val buf = buffers.peekFirst()
        if (buf == null) {
            //println("No data " + buffers.size + " available")
            return 0
        }

        println("Data " + buffers.size + " available")

        return buf.size
    }

    override fun read(buf: ByteArray): Int {
        /*if (!process.isAlive && process.inputStream.available() == 0)
            throw RuntimeException("The process ($name) is no longer alive. Exit code ${process.exitValue()}")
        return process.inputStream.read(buf)*/
        //buffer.copyInto(buf)
        println("Removing")
        val x = buffers.removeFirst()
        x.copyInto(buf)
        return x.size
    }

    override fun write(buf: ByteArray) {
        process.outputStream.write(buf)
        process.outputStream.flush()
    }

    override fun close() {
        process.destroy()
    }
}
