import org.junit.Test
import parser.LL1Parser

class LL1ParserTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }
    @Test
    fun test() {
        val parser = LL1Parser(listOf(
            LL1Parser.ProductionRuleData("E", "T E'".tokenize()),
            LL1Parser.ProductionRuleData("E'", "+ T E'".tokenize()),
            LL1Parser.ProductionRuleData("E'", "ε".tokenize()),
            LL1Parser.ProductionRuleData("T", "F T'".tokenize()),
            LL1Parser.ProductionRuleData("T'", "* F T'".tokenize()),
            LL1Parser.ProductionRuleData("T'", "ε".tokenize()),
            LL1Parser.ProductionRuleData("F", "( E )".tokenize()),
            LL1Parser.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        assert(true)
    }


    @Test
    fun test2() {
        val parser = LL1Parser(listOf(
            LL1Parser.ProductionRuleData("E", "T E'".tokenize()),
            LL1Parser.ProductionRuleData("E'", "+ T E'".tokenize()),
            LL1Parser.ProductionRuleData("E'", "ε".tokenize()),
            LL1Parser.ProductionRuleData("T", "F T'".tokenize()),
            LL1Parser.ProductionRuleData("T'", "* F T'".tokenize()),
            LL1Parser.ProductionRuleData("T'", "ε".tokenize()),
            LL1Parser.ProductionRuleData("F", "( E )".tokenize()),
            LL1Parser.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        println("g: ${parser.generateTable()}")
        assert(true)
    }
}