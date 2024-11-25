package debugger

import woodstate.IOState
import java.io.File

data class Override(val primName: String, val arg: Int, val returnValue: Int)
data class Relation(val io: IOState, val override: Override, val comp: (a: Int, b: Int) -> Boolean)

class ConstraintParser(private val tokens: List<Token>) {
    private var i = 0
    private val currentToken: Token
        get() = tokens[i]

    private fun expectToken(type: TokenType): Token {
        return expectTokens(listOf(type))
    }

    private fun expectTokens(types: List<TokenType>): Token {
        if (!types.contains(currentToken.type)) {
            throw RuntimeException("Expected token to be part of $types but got ${currentToken.type}")
        } else {
            val result = currentToken
            i++
            return result
        }
    }

    companion object {
        fun parse(str: String): Relation {
            return ConstraintParser(tokenize(str)).parseRelation()
        }

        fun parseFile(filename: String): List<Relation> {
            val constraints = mutableListOf<Relation>()
            File(filename).forEachLine {
                constraints.add(parse(it))
            }
            return constraints
        }
    }

    /*
     * p45 == 0 => color_sensor(0) = 0
     */
    private fun parseRelation(): Relation {
        val lhs = parseLhs()
        expectToken(TokenType.IMPLIES)
        val override = parseRhs()
        return Relation(lhs.first, override, lhs.second)
    }

    private fun parseLhs(): Pair<IOState, (a: Int, b: Int) -> Boolean> {
        val keyToken = expectToken(TokenType.VALUE).lexeme
        val compOperator = expectTokens(listOf(TokenType.COMP_EQUALS, TokenType.COMP_SMALLER, TokenType.COMP_BIGGER)).type
        val comp = when (compOperator) {
            TokenType.COMP_EQUALS -> { a: Int, b: Int -> a == b}
            TokenType.COMP_SMALLER -> { a: Int, b: Int -> a < b}
            TokenType.COMP_BIGGER -> { a: Int, b: Int -> a > b}
            else -> throw RuntimeException("Unknown operator $compOperator")
        }
        val value = expectToken(TokenType.VALUE).lexeme.toInt()
        println("Compare $keyToken, $value")
        return Pair(IOState(keyToken, true, value), comp)
    }

    private fun parseRhs(): Override {
        val primName = expectToken(TokenType.VALUE).lexeme
        expectToken(TokenType.LPAREN)
        val arg = expectToken(TokenType.VALUE).lexeme.toInt()
        expectToken(TokenType.RPAREN)
        expectToken(TokenType.EQUALS)
        val result = expectToken(TokenType.VALUE).lexeme.toInt()
        return Override(primName, arg, result)
    }
}

enum class TokenType {
    COMP_EQUALS,
    COMP_SMALLER,
    COMP_BIGGER,
    EQUALS,
    IMPLIES,
    LPAREN,
    RPAREN,
    VALUE
}
data class Token(val type: TokenType, val lexeme: String)

fun tokenize(str: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    var lexeme = ""

    fun eat() {
        lexeme += str[i]
        i++
    }

    fun pushToken(type: TokenType) {
        tokens.add(Token(type, lexeme))
        lexeme = ""
    }

    fun currentToken(): Char {
        return str[i]
    }

    while (i < str.length) {
        when (str[i]) {
            ' ' -> {
                eat()
                lexeme = ""
            }
            '=' -> {
                eat()
                if (currentToken() == '=') {
                    eat()
                    pushToken(TokenType.COMP_EQUALS)
                }
                else if (currentToken() == '>') {
                    eat()
                    pushToken(TokenType.IMPLIES)
                }
                else {
                    pushToken(TokenType.EQUALS)
                }
            }
            '<' -> {
                eat()
                pushToken(TokenType.COMP_SMALLER)
            }
            '>' -> {
                eat()
                pushToken(TokenType.COMP_BIGGER)
            }
            '(' -> {
                eat()
                pushToken(TokenType.LPAREN)
            }
            ')' -> {
                eat()
                pushToken(TokenType.RPAREN)
            }
            '#' -> {
                return tokens
            }
            '/' -> {
                eat()
                if (currentToken() == '/') {
                    return tokens
                }
            }
            else -> {
                while (str[i].isLetterOrDigit() || str[i] == '_') {
                    eat()
                }
                pushToken(TokenType.VALUE)
            }
        }
    }
    return tokens
}
