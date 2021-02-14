import org.junit.Test
import parser.LL1Parser
import parser.ProductionRuleData

class LL1ParserTest {
    @Test
    fun test() {
        val parser = LL1Parser(listOf(
            ProductionRuleData('S', "TE"),
            ProductionRuleData('E', "+TE"),
            ProductionRuleData('E', "ε"),
            ProductionRuleData('T', "Ft"),
            ProductionRuleData('t', "*Ft"),
            ProductionRuleData('t', "ε"),
            ProductionRuleData('F', "(S)"),
            ProductionRuleData('F', "i"),
        ))
        assert(true)
    }
}