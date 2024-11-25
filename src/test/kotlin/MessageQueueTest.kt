import debugger.MessageQueue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import woodstate.WOODState
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

private const val snapshotJsonExample1 = "{\"pc\":156,\"breakpoints\":[0],\"callstack\":[{\"type\":0,\"fidx\":\"0x4\",\"sp\":-1,\"fp\":-1,\"idx\":0,\"block_key\":0,\"ra\":111},{\"type\":3,\"fidx\":\"0x0\",\"sp\":0,\"fp\":0,\"idx\":1,\"block_key\":144,\"ra\":146}],\"globals\":[{\"idx\":0,\"type\":\"i32\",\"value\":26},{\"idx\":1,\"type\":\"i32\",\"value\":1},{\"idx\":2,\"type\":\"i32\",\"value\":0}],\"table\":{\"max\":0, \"init\":0, \"elements\":[]},\"memory\":{\"pages\":0,\"max\":0,\"init\":0,\"bytes\":[]},\"br_table\":{\"size\":\"0x100\",\"labels\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]},\"stack\":[{\"idx\":0,\"type\":\"i32\",\"value\":1000}],\"callbacks\": [],\"events\": []}"
private const val snapshotJsonExample2 = "{\"pc\":157,\"breakpoints\":[0],\"callstack\":[{\"type\":0,\"fidx\":\"0x4\",\"sp\":-1,\"fp\":-1,\"idx\":0,\"block_key\":0,\"ra\":111},{\"type\":3,\"fidx\":\"0x0\",\"sp\":0,\"fp\":0,\"idx\":1,\"block_key\":144,\"ra\":146}],\"globals\":[{\"idx\":0,\"type\":\"i32\",\"value\":26},{\"idx\":1,\"type\":\"i32\",\"value\":1},{\"idx\":2,\"type\":\"i32\",\"value\":0}],\"table\":{\"max\":0, \"init\":0, \"elements\":[]},\"memory\":{\"pages\":0,\"max\":0,\"init\":0,\"bytes\":[]},\"br_table\":{\"size\":\"0x100\",\"labels\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]},\"stack\":[{\"idx\":0,\"type\":\"i32\",\"value\":1000}],\"callbacks\": [],\"events\": []}"

class MessageQueueTest {
    @Test
    fun `Test if the MessageQueue returns responses in the correct order`() {
        val queue = MessageQueue()
        queue.push("test\n")
        queue.push(snapshotJsonExample1)
        queue.push("\n")
        queue.push(snapshotJsonExample2)
        queue.push("\n")
        queue.push("test\n")

        assertEquals(snapshotJsonExample1, queue.waitForResponse {
            WOODState.fromLine(it)
        }.first)
        assertEquals(snapshotJsonExample2, queue.waitForResponse {
            WOODState.fromLine(it)
        }.first)
    }

    @ParameterizedTest
    @ValueSource(strings = ["test\n", snapshotJsonExample1])
    fun `Test if waitForResponse keeps waiting when there is no valid response`(msg: String) {
        val queue = MessageQueue()
        queue.push("test\n")
        queue.push(msg)
        val t = thread {
            queue.waitForResponse {
                WOODState.fromLine(it)
            }
        }
        // Give it a certain amount of  time to execute the block of code in the thread, long enough for it to finish
        // if a valid response was in the queue.
        t.join(1000)
        assertEquals(true, t.isAlive)
    }
}
