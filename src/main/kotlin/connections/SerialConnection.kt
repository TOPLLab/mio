package connections

import com.fazecast.jSerialComm.SerialPort

class SerialConnection(port: String, baudRate: Int = 115200) : Connection {
    private val serial: SerialPort = SerialPort.getCommPort(port)

    init {
        serial.setBaudRate(baudRate)
        val success = serial.openPort()
        if (!success) {
            throw Exception("Could not open port \"$port\"!")
        }
    }

    override fun bytesAvailable(): Int {
        return serial.bytesAvailable()
    }

    override fun read(buf: ByteArray): Int {
        return serial.readBytes(buf, buf.size)
    }

    override fun write(buf: ByteArray) {
        serial.writeBytes(buf, buf.size)
    }

    override fun close() {
        serial.closePort()
    }
}
