import org.junit.Test
import parser.LR0ParserGenerator
import parser.SLR1Parser
import parser.SLR1ParserGenerator

class SLR1ParserGeneratorTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }

    @Test
    fun isSLR1Test() {
        val generator = SLR1ParserGenerator(listOf(
            SLR1ParserGenerator.ProductionRuleData("E", "E + T".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("E", "T".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("T", "T * F".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("T", "F".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        generator.printClosureMap()
        generator.printGotoMap()
        generator.printTransitionMap()
        assert(true)
    }

    @Test
    fun parseTest() {
        val generator = SLR1ParserGenerator(listOf(
            SLR1ParserGenerator.ProductionRuleData("E", "E + T".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("E", "T".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("T", "T * F".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("T", "F".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            SLR1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        generator.printClosureMap()
        generator.printGotoMap()
        generator.printTransitionMap()
        val parser = SLR1Parser(generator.transitionMap)
        val node = parser.parse("i * i + i $".tokenize())
        assert(node != null)
        node?.print("")
        assert(true)
    }
}