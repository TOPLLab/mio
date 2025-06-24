package be.ugent.topl.mio.debugger

import java.util.*
import java.util.concurrent.locks.ReentrantLock

class MessageQueue {
    private val queue = Collections.synchronizedList(mutableListOf<String>())
    private val removeLock = ReentrantLock()

    fun push(data: String, keepLock: Boolean = false) {
        val splitData = data.split("\n").toMutableList()
        removeLock.lock()
        if (lastMessageIncomplete()) {
            queue.add(queue.removeLast() + splitData.removeFirst())
        }
        if (!keepLock) {
            removeLock.unlock()
        }
        queue.addAll(splitData)
    }

    fun pushDone() {
        removeLock.unlock()
    }

    private fun pop(): String {
        return queue.removeFirst()
    }

    private fun lastMessageIncomplete(): Boolean {
        return !(queue.lastOrNull()?.endsWith("\n") ?: true)
    }

    private fun hasCompleteMessage(): Boolean {
        return queue.isNotEmpty() && (!lastMessageIncomplete() || queue.size > 1)
    }

    /**
     * Takes a [parser] as an input, it searches the message queue until it finds a message that the parser can parse
     * without throwing an exception.
     *
     * TODO: This is not really an ideal implementation, it would be nice if responses were of a form:
     * ```
     * ack $interruptNumber;$payload
     * ```
     */
    private fun <T> searchHacky(parser: (String) -> T): Pair<String, T>? {
        removeLock.lock()
        while (hasCompleteMessage()) {
            val currentMsg = pop()
            try {
                val parsedMsg = parser(currentMsg)
                removeLock.unlock()
                return Pair(currentMsg, parsedMsg)
            }
            catch (_: Exception) {}
        }

        removeLock.unlock()
        return null
    }

    fun <T> search(parser: (String) -> T): Pair<String, T>? {
        removeLock.lock()
        val iter = queue.iterator()
        while (iter.hasNext()) {
            val msg = iter.next()

            if (msg === queue.last() && !lastMessageIncomplete() || msg !== queue.last()) {
                try {
                    val parsed = parser(msg)
                    iter.remove()
                    //println("removing $msg")
                    removeLock.unlock()
                    return Pair(msg, parsed)
                }
                catch (_: Exception) {}
            }
        }

        removeLock.unlock()
        return null;
    }

    fun <T> waitForResponse(parser: (String) -> T): Pair<String, T> {
        var result = searchHacky(parser)
        while(result == null) {
            Thread.sleep(20)
            result = searchHacky(parser)
        }
        return result
    }

    fun <T> searchForResponse(parser: (String) -> T): Pair<String, T> {
        var result = search(parser)
        while(result == null) {
            Thread.sleep(20)
            result = search (parser)
        }
        return result
    }

    fun waitForResponse(str: String) {
        waitForResponse {
            if (it.trimEnd('\r') != str) throw Exception()
        }
    }
}
