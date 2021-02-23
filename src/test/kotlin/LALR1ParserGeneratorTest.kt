import org.junit.Test
import parser.LALR1ParserGenerator
import parser.LR1ParserGenerator

class LALR1ParserGeneratorTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }
    @Test
    fun test() {
        val parser = LALR1ParserGenerator(listOf(
            LR1ParserGenerator.ProductionRuleData("E", "E + T".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "T".tokenize()),
            LR1ParserGenerator.ProductionRuleData("T", "T * F".tokenize()),
            LR1ParserGenerator.ProductionRuleData("T", "F".tokenize()),
            LR1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            LR1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        parser.lr1ParserGenerator.printClosureMap()
        parser.lr1ParserGenerator.printGotoMap()
        parser.printClosureMap()
        parser.printGotoMap()
        assert(true)
    }

    @Test
    fun test2() {
        val parser = LALR1ParserGenerator(listOf(
            LR1ParserGenerator.ProductionRuleData("A", "L = E".tokenize()),
            LR1ParserGenerator.ProductionRuleData("L", "i".tokenize()),
            LR1ParserGenerator.ProductionRuleData("L", "R ↑ i".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "E + R".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "R".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "@ L".tokenize()),
            LR1ParserGenerator.ProductionRuleData("R", "i".tokenize()),
        ), "A")
        parser.lr1ParserGenerator.printClosureMap()
        parser.lr1ParserGenerator.printGotoMap()
        parser.printClosureMap()
        parser.printGotoMap()
        assert(true)
    }

    @Test
    fun test3() {
        val parser = LALR1ParserGenerator(listOf(
            LR1ParserGenerator.ProductionRuleData("A", "L = E".tokenize()),
            LR1ParserGenerator.ProductionRuleData("L", "i".tokenize()),
            LR1ParserGenerator.ProductionRuleData("L", "R ↑ i".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "E + R".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "R".tokenize()),
            LR1ParserGenerator.ProductionRuleData("E", "@ L".tokenize()),
            LR1ParserGenerator.ProductionRuleData("R", "i".tokenize()),
        ), "A")
        parser.lr1ParserGenerator.printClosureMap()
        parser.lr1ParserGenerator.printGotoMap()
        parser.printClosureMap()
        parser.printGotoMap()
        parser.printTransitionMap()
        assert(true)
    }
}