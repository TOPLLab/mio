import be.ugent.topl.mio.debugger.Debugger
import org.junit.jupiter.api.Test
import be.ugent.topl.mio.woodstate.WOODDumpResponse
import be.ugent.topl.mio.woodstate.compressRLE
import kotlin.random.Random
import kotlin.test.assertEquals

class RLETests : EmulatorTestBase() {
    private fun loadAndRestore(debugger: Debugger, mutator: (s: WOODDumpResponse) -> Unit) {
        val snapshot = debugger.snapshotFull().second
        mutator(snapshot)
        debugger.loadSnapshot(snapshot)
        val after = debugger.snapshotFull().second
        for (i in snapshot.memory!!.bytes.indices) {
            assertEquals(snapshot.memory!!.bytes[i], after.memory!!.bytes[i], "Memory does not match at position $i")
        }
    }

    @Test
    fun `Test memory restore` () {
        runWithDebugger("prime/prime.wasm", true) {
            loadAndRestore(it) {}
            loadAndRestore(it) { snapshot ->
                snapshot.memory!!.bytes[100] = 5
                snapshot.memory!!.bytes[7] = 3
            }
            val random = Random(42)
            loadAndRestore(it) { snapshot ->
                repeat(20) {
                    snapshot.memory!!.bytes[random.nextInt(snapshot.memory!!.bytes.size)] = random.nextInt(0xff).toByte()
                }
            }
            loadAndRestore(it) { snapshot ->
                repeat(1000) {
                    snapshot.memory!!.bytes[random.nextInt(snapshot.memory!!.bytes.size)] = random.nextInt(0xff).toByte()
                }
            }
            loadAndRestore(it) { snapshot ->
                random.nextBytes(snapshot.memory!!.bytes)
            }
        }
    }

    @Test
    fun `Run length test`() {
        assertEquals("01050200010103050107", compressRLE("0500000105050507"))
        assertEquals("020502000101030502060103", compressRLE("0505000001050505060603"))
    }
}