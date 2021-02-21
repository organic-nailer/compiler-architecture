import org.junit.Test
import parser.LL1ParserGenerator
import parser.LR0Parser
import parser.LR0ParserGenerator

class LR0ParserGeneratorTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }
    @Test
    fun nonLR0Test() {
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
        assert(!parser.isLR0Grammar())
    }

    @Test
    fun isLR0Test() {
        val parser = LR0ParserGenerator(listOf(
            LR0ParserGenerator.ProductionRuleData("S", "( L )".tokenize()),
            LR0ParserGenerator.ProductionRuleData("S", "a".tokenize()),
            LR0ParserGenerator.ProductionRuleData("L", "L , S".tokenize()),
            LR0ParserGenerator.ProductionRuleData("L", "S".tokenize()),
        ), "S")
        parser.printClosureMap()
        parser.printGotoMap()
        assert(parser.isLR0Grammar())
    }

    @Test
    fun parseTest() {
        val generator = LR0ParserGenerator(listOf(
            LR0ParserGenerator.ProductionRuleData("S", "( L )".tokenize()),
            LR0ParserGenerator.ProductionRuleData("S", "a".tokenize()),
            LR0ParserGenerator.ProductionRuleData("L", "L , S".tokenize()),
            LR0ParserGenerator.ProductionRuleData("L", "S".tokenize()),
        ), "S")
        generator.printClosureMap()
        generator.printGotoMap()
        val parser = LR0Parser(
            generator.gotoMap, generator.closureMap
        )
        val node = parser.parse("( a , a , a ) $".tokenize())
        assert(node != null)
        node?.print("")
        assert(true)
    }
}
