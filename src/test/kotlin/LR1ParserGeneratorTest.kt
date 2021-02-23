import org.junit.Test
import parser.LR0ParserGenerator
import parser.LR1ParserGenerator

class LR1ParserGeneratorTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }
    @Test
    fun test() {
        val parser = LR1ParserGenerator(listOf(
            LR1ParserGenerator.ProductionRuleData("E", "E + T".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "T".tokenize()),
            LR1ParserGenerator.ProductionRuleData("T", "T * F".tokenize()),
            LR1ParserGenerator.ProductionRuleData("T", "F".tokenize()),
            LR1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            LR1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        parser.printClosureMap()
        parser.printGotoMap()
        assert(true)
    }
}