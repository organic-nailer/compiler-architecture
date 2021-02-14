import java.io.BufferedReader
import java.lang.Exception

class CharReader(
    private val reader: BufferedReader
) {
    companion object {
        private const val EOFInt = 26
        val EOF = EOFInt.toChar()
    }

    private var line = ""
    private var lineLength = 0
    private var index = 1

    fun getNextChar(): Char {
        if(index < lineLength) {
            return line[index++]
        }
        if(index++ == lineLength) {
            return '\n'
        }

        try {
            line = reader.readLine()
            if(line != null) {
                lineLength = line.length
                index = 0
                println(line)
                return getNextChar()
            }
        } catch(e: Exception) {
            println(e.message)
        }
        return EOF
    }
}