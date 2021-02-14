import java.io.BufferedReader
import java.lang.Exception

class TokenReader(
    reader: BufferedReader
) {
    companion object {
        private const val MAX_TOKEN_LENGTH = 32
        enum class CharKind {
            OTHERS, DIGIT, LETTER, SPACE, SEMICOLON, NEW_LINE, EOF
        }
    }

    private val charReader = CharReader(reader)
    private val sBuffer = StringBuffer(MAX_TOKEN_LENGTH)
    private var currentChar = '\n'

    private fun getCharKind(c: Char): CharKind {
        if(c.isDigit()) return CharKind.DIGIT
        if(c.isLetter()) return CharKind.LETTER
        if(c == ';') return CharKind.SEMICOLON
        if(c == ' ') return CharKind.SPACE
        if(c == '\n') return CharKind.NEW_LINE
        if(c == '\u001a') return CharKind.EOF
        return CharKind.OTHERS
    }

    fun nextToken(): String {
        sBuffer.setLength(0)
        var nextState = 1
        while(getCharKind(currentChar) == CharKind.SPACE
            || getCharKind(currentChar) == CharKind.NEW_LINE) {
            currentChar = charReader.getNextChar()
        }
        while(true) {
            when(nextState) {
                1 -> {
                    when(getCharKind(currentChar)) {
                        CharKind.LETTER -> {
                            sBuffer.append(currentChar)
                            nextState = 2
                            continue
                        }
                        CharKind.DIGIT -> {
                            sBuffer.append(currentChar)
                            nextState = 3
                            continue
                        }
                        CharKind.EOF -> {
                            sBuffer.append(currentChar)
                            break
                        }
                        CharKind.SEMICOLON -> {
                            sBuffer.append(currentChar)
                            nextState = 4
                            continue
                        }
                        else -> { throw Exception("解決不可能なトークン $currentChar") }
                    }
                }
                2 -> {
                    currentChar = charReader.getNextChar()
                    if(getCharKind(currentChar) == CharKind.LETTER
                        || getCharKind(currentChar) == CharKind.DIGIT) {
                        sBuffer.append(currentChar)
                        nextState = 2
                        continue
                    }
                    break
                }
                3 -> {
                    currentChar = charReader.getNextChar()
                    if(getCharKind(currentChar) == CharKind.DIGIT) {
                        sBuffer.append(currentChar)
                        nextState = 3
                        continue
                    }
                    break
                }
                4 -> {
                    currentChar = charReader.getNextChar()
                    break
                }
            }
        }
        return sBuffer.toString()
    }
}
