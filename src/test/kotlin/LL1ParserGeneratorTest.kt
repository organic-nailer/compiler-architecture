import org.junit.Test
import parser.LL1Parser
import parser.LL1ParserGenerator

class LL1ParserGeneratorTest {
    fun String.tokenize(): List<String> {
        return this.split(" ")
    }
    @Test
    fun test() {
        val parser = LL1ParserGenerator(listOf(
            LL1ParserGenerator.ProductionRuleData("E", "T E'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("E'", "+ T E'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("E'", "ε".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T", "F T'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T'", "* F T'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T'", "ε".tokenize()),
            LL1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            LL1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        assert(true)
    }


    @Test
    fun test2() {
        val parser = LL1ParserGenerator(listOf(
            LL1ParserGenerator.ProductionRuleData("E", "T E'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("E'", "+ T E'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("E'", "ε".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T", "F T'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T'", "* F T'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T'", "ε".tokenize()),
            LL1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            LL1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        println("g: ${parser.generateTable()}")
        assert(true)
    }

    @Test
    fun test3() {
        val parserGenerator = LL1ParserGenerator(listOf(
            LL1ParserGenerator.ProductionRuleData("E", "T E'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("E'", "+ T E'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("E'", "ε".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T", "F T'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T'", "* F T'".tokenize()),
            LL1ParserGenerator.ProductionRuleData("T'", "ε".tokenize()),
            LL1ParserGenerator.ProductionRuleData("F", "( E )".tokenize()),
            LL1ParserGenerator.ProductionRuleData("F", "i".tokenize()),
        ), "E")
        println("g: ${parserGenerator.generateTable()}")
        val table = parserGenerator.generateTable() ?: return
        val parser = LL1Parser(table, parserGenerator.terminalTokens, parserGenerator.nonTerminalTokens, "E")
        val node = parser.parse("i*i+i$")
        node?.let { printNode(it, "") }
        assert(true)
    }

    private fun printNode(node: LL1Parser.Node, indent: String) {
        println(indent + node.kind)
        node.children.forEach { printNode(it, "$indent  ") }
    }
}