import org.junit.jupiter.api.Test
import woodstate.HexaEncoder
import kotlin.test.assertEquals

class HexaEncoderTest {
    @Test
    fun `Test LEB128`() {
        assertEquals("E58E26", HexaEncoder.convertToLEB128(624485))
    }

    @Test
    fun `Test length of serializeBigUInt64`() {
        // 8 bytes
        assertEquals(8 * 2, HexaEncoder.serializeBigUInt64(0, true).length)
    }

    @Test
    fun `Test length of serializeInt32`() {
        // 4 bytes
        assertEquals(4 * 2, HexaEncoder.serializeInt32(0, true).length)
    }
}
