import org.junit.Test
import org.junit.Assert.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class CharReaderTest {
    @Test
    fun readFirstChar() {
        val file = File("src\\test\\kotlin\\CharReaderTest.txt")
        val fileReader = FileReader(file)
        val reader = BufferedReader(fileReader)
        val charReader = CharReader(reader)
        assertEquals(charReader.getNextChar(), 'H')
        reader.close()
    }

    @Test
    fun readChars() {
        val file = File("src\\test\\kotlin\\CharReaderTest.txt")
        val fileReader = FileReader(file)
        val reader = BufferedReader(fileReader)
        val charReader = CharReader(reader)
        val gotChars = mutableListOf<Char>()
        for(i in 1..6) {
            gotChars.add(charReader.getNextChar())
        }
        assertArrayEquals(gotChars.toCharArray(), charArrayOf('H','e','l','l','o',','))
        reader.close()
    }
}
