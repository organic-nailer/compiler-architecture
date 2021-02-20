package parser

import java.lang.Exception

class LR0ParserGenerator(
    private val rules: List<ProductionRuleData>,
    private val startSymbol: String) {

    companion object {
        private const val EMPTY = "ε"
    }

    val terminalTokens = mutableListOf<String>()
    val nonTerminalTokens = mutableListOf<String>()
    val gotoMap = mutableMapOf<Pair<String, String>, String>()
    val closureMap = mutableMapOf<Set<LRProductionRuleData>, String>()

    init {
        calcTokenKind()
        calcGoto()
    }

    private fun calcTokenKind() {
        println("CalcTokenKind")
        terminalTokens.clear()
        nonTerminalTokens.clear()
        nonTerminalTokens.addAll(rules.map { r -> r.left }.distinct())
        rules.forEach { r ->
            r.right.forEach { c ->
                if(!nonTerminalTokens.contains(c) && !terminalTokens.contains(c)) {
                    terminalTokens.add(c)
                }
            }
        }
        terminalTokens.remove(EMPTY)
        println("T=$terminalTokens")
        println("N=$nonTerminalTokens")
    }

    private fun calcGoto() {
        val initialGrammar = ProductionRuleData("$startSymbol'", listOf(startSymbol, "$"))
        val extendedRules = rules.toMutableList().apply {
            add(initialGrammar)
        }
        closureMap.clear()
        var closureIndex = 0
        closureMap[getClosure(setOf(initialGrammar.toLR()), extendedRules)] = "I${closureIndex++}"
        var updated = true
        while(updated) {
            updated = false
            for(entry in closureMap.toMap()) {
                val transitionalTokens = entry.key
                    .filter { r -> !r.reducible }
                    .map { r -> r.right[r.index] }
                    .distinct()
                for(t in transitionalTokens) {
                    if(gotoMap.containsKey(entry.value to t)) continue
                    updated = true
                    val gotoSet = getGoto(entry.key, t, extendedRules)
                    if(gotoSet.isNotEmpty()) {
                        closureMap[gotoSet]?.let {
                            gotoMap[entry.value to t] = it
                        } ?: kotlin.run {
                            val label = "I${closureIndex++}"
                            closureMap[gotoSet] = label
                            gotoMap[entry.value to t] = label
                        }
                    }
                }
            }
        }
    }

    private fun getClosure(
        input: Set<LRProductionRuleData>,
        grammarRules: List<ProductionRuleData> = rules
    ): Set<LRProductionRuleData> {
        println("getClosure: $input")
        val result = input.toMutableSet()
        val addedTokens = mutableSetOf<String>()
        var updated = true
        while(updated) {
            updated = false
            for(rule in result) {
                if(rule.reducible) continue
                val target = rule.right[rule.index]
                if(!addedTokens.contains(target)
                    && nonTerminalTokens.contains(target)) {
                    addedTokens.add(target)
                    val u = result.addAll(grammarRules.filter { r -> r.left == target }.map { r -> r.toLR() })
                    if(u) updated = true
                }
            }
        }
        println("result: $result")
        return result
    }

    private fun getGoto(
        input: Set<LRProductionRuleData>,
        token: String,
        grammarRules: List<ProductionRuleData> = rules
    ): Set<LRProductionRuleData> {
        println("GetGoto: $input, $token")
        return getClosure(
            input
                .filter { r -> !r.reducible && r.right[r.index] == token }
                .map { r -> r.shift() }
                .toSet(),
            grammarRules
        )
    }

    fun printClosureMap() {
        println("Closures")
        closureMap.forEach { t, u ->
            println("${u.padEnd(3)}: $t")
        }
    }

    fun printGotoMap() {
        println("Goto Table")
        print("   ")
        terminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("  $  ")
        nonTerminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("\n")
        for(c in closureMap.values) {
            print(c.padEnd(3))
            terminalTokens.forEach { print(" ${gotoMap[c to it]?.padStart(3) ?: "   "} ") }
            print(" ${gotoMap[c to "$"]?.padStart(3) ?: "   "} ")
            nonTerminalTokens.forEach { print(" ${gotoMap[c to it]?.padStart(3) ?: "   "} ") }
            print("\n")
        }
    }

    data class LRProductionRuleData(
        val left: String,
        val right: List<String>,
        val index: Int,
        val reducible: Boolean
    ) {
        fun shift(): LRProductionRuleData {
            if(reducible) throw Exception("シフトできません $this")
            return LRProductionRuleData(
                left, right, index+1,
                reducible = index+1 >= right.size
            )
        }

        override fun toString(): String {
            return "$left->${right.toMutableList().apply { add(index,"・") }.joinToString("")}"
        }
    }

    data class ProductionRuleData(
        val left: String,
        val right: List<String> //tokenized
    ) {
        fun toLR() = LRProductionRuleData(
            left = left, right = right,
            index = 0, reducible = right.size == 1 && right[0] == EMPTY
        )
    }
}