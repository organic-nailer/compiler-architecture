package parser

import java.lang.Exception

class LR1ParserGenerator(
    private val rules: List<ProductionRuleData>,
    private val startSymbol: String) {
    companion object {
        private const val EMPTY = "ε"
    }

    val terminalTokens = mutableListOf<String>()
    val nonTerminalTokens = mutableListOf<String>()
    val firstMap = mutableMapOf<String, MutableSet<String>>()
    val gotoMap = mutableMapOf<Pair<String, String>, String>()
    val closureMap = mutableMapOf<Set<LR1ProductionRuleData>, String>()

    init {
        calcTokenKind()
        calcFirst()
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

    private fun getFirst(value: List<String>): Set<String> {
        if(value.isEmpty()) return setOf()
        if(terminalTokens.contains(value.first()) || value.first() == EMPTY || value.first() == "$") {
            return setOf(value.first())//{α} or {ε}
        }
        firstMap[value.first()]?.let {
            if(!it.contains(EMPTY) || value.size == 1) {
                return it //First(Y)
            }
            return it.minus(EMPTY).union(getFirst(value.slice(1 until value.size))) //(First(Y)-{ε})∪First(α)
        }
        return emptySet()
    }

    private fun addFirst(key: String, set: Set<String>) {
        firstMap[key]?.addAll(set) ?: kotlin.run {
            firstMap[key] = set.toMutableSet()
        }
    }

    private fun calcFirst() { //First集合を計算する
        var updated = true
        while(updated) {
            updated = false
            for(rule in rules) {
                val firstAlpha = getFirst(rule.right)
                val firstX = firstMap[rule.left] ?: emptySet()
                val diff = firstAlpha.minus(firstX)
                if(diff.isNotEmpty()) {
                    updated = true
                    addFirst(rule.left, diff)
                }
            }
        }
    }

    private fun getClosure(
        input: Set<LR1ProductionRuleData>,
        grammarRules: List<ProductionRuleData> = rules
    ): Set<LR1ProductionRuleData> {
        println("getClosure: $input")
        val result = input.toMutableSet()
        var updated = true
        while(updated) {
            updated = false
            for(rule in result.toSet()) {
                if(rule.reducible) continue
                val target = rule.right[rule.index]
                if(nonTerminalTokens.contains(target)) {
                    val firstSet = getFirst(
                        rule.right.slice(rule.index+1 until rule.right.size).toMutableList().apply { add(rule.follow) }
                    )
                    firstSet.forEach { f ->
                        val u = result.addAll(grammarRules.filter { r -> r.left == target }.map { r -> r.toLR1(f) })
                        if(u) updated = true
                    }
                }
            }
        }
        println("result: $result")
        return result
    }

    private fun calcGoto() {
        val initialGrammar = ProductionRuleData("$startSymbol'", listOf(startSymbol))
        val extendedRules = rules.toMutableList().apply {
            add(initialGrammar)
        }
        closureMap.clear()
        var closureIndex = 0
        closureMap[getClosure(setOf(initialGrammar.toLR1("$")), extendedRules)] = "I${closureIndex++}"
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

    private fun getGoto(
        input: Set<LR1ProductionRuleData>,
        token: String,
        grammarRules: List<ProductionRuleData> = rules
    ): Set<LR1ProductionRuleData> {
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

    data class LR1ProductionRuleData(
        val left: String,
        val right: List<String>,
        val index: Int,
        val reducible: Boolean,
        val follow: String
    ) {
        fun shift(): LR1ProductionRuleData {
            if(reducible) throw Exception("シフトできません $this")
            return LR1ProductionRuleData(
                left, right, index+1,
                reducible = index+1 >= right.size,
                follow
            )
        }

        override fun toString(): String {
            return "$left->${right.toMutableList().apply { add(index,"・") }.joinToString("")} - $follow"
        }
    }

    data class ProductionRuleData(
        val left: String,
        val right: List<String> //tokenized
    ) {
        fun toLR1(follow: String) = LR1ProductionRuleData(
            left = left, right = right,
            index = 0, reducible = right.size == 1 && right[0] == EMPTY,
            follow
        )
    }
}