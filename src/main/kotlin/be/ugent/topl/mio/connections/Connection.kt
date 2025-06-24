package be.ugent.topl.mio.connections

import java.io.Closeable

interface Connection : Closeable {
    fun bytesAvailable(): Int
    fun read(buf: ByteArray): Int
    fun write(buf: ByteArray)
}
