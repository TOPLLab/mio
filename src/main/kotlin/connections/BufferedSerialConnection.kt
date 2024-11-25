package connections

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class BufferedSerialConnection(port: String, baudRate: Int = 115200) : Connection, SerialPortDataListener {
    private val serial: SerialPort = SerialPort.getCommPort(port)

    init {
        serial.setBaudRate(baudRate)
        serial.flushIOBuffers()
        serial.addDataListener(this)
        val success = serial.openPort()
        if (!success) {
            throw Exception("Could not open port \"$port\"!")
        }
        //serial.addDataListener()
    }
    //val reader = BufferedInputStream(serial.inputStream, 1024 * 128)

    override fun bytesAvailable(): Int {
        //return serial.bytesAvailable()
        if (buffer.isEmpty())
            return 0
        return buffer.first().size
        //return reader.available()
    }

    override fun read(buf: ByteArray): Int {
        /*val charBuffer = CharBuffer()
        return reader.read(buf)*/
        //return serial.readBytes(buf, buf.size)
        //return serial.inputStream.read(buf)
        if (buffer.isEmpty()) {
            ByteArray(0).copyInto(buf, 0)
            return 0
        }
        val r = buffer.removeFirst()
        r.copyInto(buf)
        return r.size

        //return reader.read(buf)
    }

    private val reentrantLock = ReentrantLock()

    override fun write(buf: ByteArray) {
        serial.writeBytes(buf, buf.size)
    }

    override fun close() {
        serial.closePort()
    }

    override fun getListeningEvents(): Int {
        //TODO("Not yet implemented")
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE
    }

    //val buffer = mutableListOf<ByteArray>()
    private val buffer: MutableList<ByteArray> = Collections.synchronizedList(mutableListOf<ByteArray>())
    override fun serialEvent(e: SerialPortEvent) {
        if (e.eventType != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return
        val newData = ByteArray(serial.bytesAvailable())
        //val numRead: Int = serial.readBytes(newData, newData.size)
        serial.readBytes(newData, newData.size)
        //assert(numRead == newData.size)
        //println("Read $numRead bytes. ${String(newData)}")
        //println("Received $numRead bytes")
        //reentrantLock.lock()
        buffer.add(newData)
        //reentrantLock.unlock()
    }
}
