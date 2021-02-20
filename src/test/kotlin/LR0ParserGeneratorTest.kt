import org.junit.Test
import parser.LL1ParserGenerator
import parser.LR0ParserGenerator

class LR0ParserGeneratorTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }
    @Test
    fun closureTest() {
        val parser = LR0ParserGenerator(listOf(
            LR0ParserGenerator.ProductionRuleData("E", "E + T".tokenize()),
            LR0ParserGenerator.ProductionRuleData("E", "T".tokenize()),
            LR0ParserGenerator.ProductionRuleData("T", "T * F".tokenize()),
            LR0ParserGenerator.ProductionRuleData("T", "F".tokenize()),
            LR0ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            LR0ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        parser.printClosureMap()
        parser.printGotoMap()
        assert(true)
    }
}