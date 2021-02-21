package parser

import kotlin.Exception

class SLR1ParserGenerator(
    private val rules: List<SLR1ParserGenerator.ProductionRuleData>,
    private val startSymbol: String) {

    companion object {
        private const val EMPTY = "ε"
    }

    val terminalTokens = mutableListOf<String>()
    val nonTerminalTokens = mutableListOf<String>()
    val firstMap = mutableMapOf<String, MutableSet<String>>()
    val followMap = mutableMapOf<String, MutableSet<String>>()
    val gotoMap = mutableMapOf<Pair<String, String>, String>()
    val closureMap = mutableMapOf<Set<LRProductionRuleData>, String>()
    val transitionMap = mutableMapOf<Pair<String, String>, TransitionData>()
    enum class TransitionKind { SHIFT, REDUCE, ACCEPT }
    data class TransitionData(
        val kind: TransitionKind,
        val value: String?,
        val rule: ProductionRuleData?
    ) {
        override fun toString(): String {
            return when(kind) {
                TransitionKind.SHIFT -> "s$value"
                TransitionKind.REDUCE -> "r${rule?.left}"
                TransitionKind.ACCEPT -> "acc"
            }
        }
    }

    init {
        calcTokenKind()
        calcGoto()
        calcFirst()
        calcFollow()
        calcTransition()
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
        if(terminalTokens.contains(value.first()) || value.first() == EMPTY) {
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
        println("CalcFirst")
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
        println("first=")
        println("$firstMap")
    }

    //return true if set is updated
    private fun addFollow(key: String, set: Set<String>): Boolean {
        println("addFollow: current=${followMap[key]}, key=$key, set=$set")
        return followMap[key]?.addAll(set) ?: kotlin.run {
            followMap[key] = set.toMutableSet()
            return@run set.isNotEmpty()
        }
    }

    private fun calcFollow() {
        println("CalcFollow")
        followMap[startSymbol] = mutableSetOf("$")
        var updated = true
        while(updated) {
            updated = false
            for(rule in rules) {
                for(i in rule.right.indices) {
                    if(nonTerminalTokens.contains(rule.right[i])) {
                        if(i >= rule.right.size - 1) {
                            val u = addFollow(rule.right[i], followMap[rule.left] ?: emptySet())
                            if(u) updated = true
                        }
                        else {
                            val firstBeta = getFirst(rule.right.slice(i+1 until rule.right.size))
                            println("First(${rule.right.slice(i+1 until rule.right.size)}) = $firstBeta")
                            var u = addFollow(rule.right[i], firstBeta.minus(EMPTY))
                            if(firstBeta.contains(EMPTY)) {
                                u = addFollow(rule.right[i], followMap[rule.left] ?: emptySet())
                            }
                            if(u) updated = true
                        }
                    }
                }
            }
            println("tochu")
            println("$followMap")
        }
        println("follow=")
        println("$followMap")
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

    private fun calcTransition() {
        for(entry in gotoMap) {
            if(terminalTokens.contains(entry.key.second) || true) {
                if(transitionMap.containsKey(entry.key)) {
                    throw Exception("SLR競合1 $entry")
                }
                transitionMap[entry.key] = TransitionData(
                    TransitionKind.SHIFT, entry.value, null
                )
            }
        }
        printTransitionMap()
        for(entry in closureMap) {
            entry.key.filter { r -> r.reducible }.forEach {
                followMap[it.left]?.forEach { token ->
                    if(transitionMap.containsKey(entry.value to token)) {
                        throw Exception("SLR競合2 $entry")
                    }
                    transitionMap[entry.value to token] = TransitionData(
                        TransitionKind.REDUCE, null, it.toRule()
                    )
                }
            }
            if(entry.key.any { r -> r.right.lastOrNull() == "$" && r.index == r.right.size - 1 }) {
//                if(transitionMap.containsKey(entry.value to "$")) {
//                    throw Exception("SLR競合3 $entry")
//                }
                transitionMap[entry.value to "$"] = TransitionData(
                    TransitionKind.ACCEPT, null, null
                )
            }
        }
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

    fun printTransitionMap() {
        println("Transition Table")
        print("   ")
        terminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("  $  ")
        nonTerminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("\n")
        for(c in closureMap.values) {
            print(c.padEnd(3))
            terminalTokens.forEach { print(" ${transitionMap[c to it]?.toString()?.padStart(3) ?: "   "} ") }
            print(" ${transitionMap[c to "$"]?.toString()?.padStart(3) ?: "   "} ")
            nonTerminalTokens.forEach { print(" ${transitionMap[c to it]?.toString()?.padStart(3) ?: "   "} ") }
            print("\n")
        }
    }

    fun isLR0Grammar(): Boolean {
        for(entry in closureMap) {
            if(entry.key.isEmpty()) continue
            if(entry.key.all { !it.reducible }) continue

            //shift/reduce競合
            if(entry.key.any { it.reducible } && entry.key.any{ !it.reducible }) return false

            //reduce/reduce競合
            if(entry.key.count { it.reducible } >= 2) return false
        }
        return true
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

        fun toRule() = ProductionRuleData(
            left, right
        )
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
