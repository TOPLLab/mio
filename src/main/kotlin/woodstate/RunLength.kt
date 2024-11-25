package woodstate

import java.lang.Integer.min

private fun getByteFromString(byteString: String, i: Int): String {
    return byteString.slice(i*2..< i*2 + 2)
}

fun compressRLE(byteString: List<String>, sizeLimit: Int): Pair<List<String>, Int> {
    var result = ""
    var count = 0
    var currentByte = byteString[0]

    var i = 0
    while (
        i < byteString.size &&
        result.length + (HexaEncoder.convertToLEB128(count) + currentByte).length + 4 <= sizeLimit // + 4 for HexaEncoder.convertToLEB128(1) + currentByte
    ) {
        if (byteString[i] == currentByte) {
            count++
        } else {
            result += HexaEncoder.convertToLEB128(count) + currentByte
            //println("$count x $currentByte")
            currentByte = byteString[i]
            count = 1
        }
        i++
    }

    result += HexaEncoder.convertToLEB128(count) + currentByte
    println("Compressed result size = ${result.length}")
    return Pair(result.chunked(2), i)
}

fun compressRLE(byteString: List<String>): List<String> {
    var result = ""
    var count = 0
    var currentByte = byteString[0]
    for (i in byteString.indices) {
        if (byteString[i] == currentByte) {
            count++
        } else {
            result += HexaEncoder.convertToLEB128(count) + currentByte
            //println("$count x $currentByte")
            currentByte = byteString[i]
            count = 1
        }
    }
    //println("$count x $currentByte")
    result += HexaEncoder.convertToLEB128(count) + currentByte
    return result.chunked(2)
}

fun compressRLE(byteString: String): String {
    var result = ""
    var count = 0
    var currentByte = getByteFromString(byteString, 0)
    for (i in 0 ..< byteString.length/2) {
        if (getByteFromString(byteString, i) == currentByte) {
            count++
        } else {
            result += HexaEncoder.convertToLEB128(count) + currentByte
            //println("$count x $currentByte")
            currentByte = getByteFromString(byteString, i)
            count = 1
        }
    }
    //println("$count x $currentByte")
    result += HexaEncoder.convertToLEB128(count) + currentByte
    return result
}