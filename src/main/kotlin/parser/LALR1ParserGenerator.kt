package parser

import java.lang.Exception

class LALR1ParserGenerator(
    rules: List<LR1ParserGenerator.ProductionRuleData>,
    private val startSymbol: String) {

    val gotoMap = mutableMapOf<Pair<String, String>, String>()
    val closureMap = mutableMapOf<Set<LALR1ProductionRuleData>, String>()
    val lr1ParserGenerator: LR1ParserGenerator = LR1ParserGenerator(rules, startSymbol)

    val transitionMap = mutableMapOf<Pair<String, String>, TransitionData>()
    enum class TransitionKind { SHIFT, REDUCE, ACCEPT }
    data class TransitionData(
        val kind: TransitionKind,
        val value: String?,
        val rule: LR1ParserGenerator.ProductionRuleData?
    ) {
        override fun toString(): String {
            return when(kind) {
                TransitionKind.SHIFT -> "s${value?.substring(1)}"
                TransitionKind.REDUCE -> "r${rule?.left}"
                TransitionKind.ACCEPT -> "acc"
            }
        }
    }

    init {
        calcLALR1Map()
        calcTransition()
    }

    private fun calcLALR1Map() {
        val ruleGroups = mutableListOf<LR1RuleCoreGroup>()
        for(entry in lr1ParserGenerator.closureMap) {
            val cores = entry.key.map { r -> LR1RuleCore(r.left, r.right, r.index, r.reducible) }.toSet()
            val match = ruleGroups.firstOrNull { r -> r.cores == cores }
            if(match != null) {
                match.states.add(entry.value)
            }
            else {
                ruleGroups.add(LR1RuleCoreGroup(
                    cores, mutableListOf(entry.value)
                ))
            }
        }
        ruleGroups.sortBy { r -> r.states.minOrNull() } //数字の若い順に
        val closureMapStateKey = lr1ParserGenerator.closureMap.toList().map { it.second to it.first }.toMap()
        val gotoTransformData = mutableMapOf<String, String>()
        ruleGroups.forEachIndexed { index, group ->
            val newState = "J$index"
            for(state in group.states) {
                gotoTransformData[state] = newState
            }
            val ruleData = group.cores.map { c ->
                val follows = mutableSetOf<String>()
                group.states.mapNotNull { s -> closureMapStateKey[s]
                    ?.filter { r -> r.left == c.left && r.right == c.right && r.index == c.index }
                }.forEach { rs ->
                    rs.forEach { r -> follows.add(r.follow) }
                }
                LALR1ProductionRuleData(
                    c.left, c.right, c.index, c.reducible,
                    follows.toList()
                )
            }.toSet()
            closureMap[ruleData] = newState
        }
        for(entry in lr1ParserGenerator.gotoMap) {
            val newState = gotoTransformData[entry.key.first] ?: continue
            val toNewState = gotoTransformData[entry.value] ?: continue
            gotoMap[newState to entry.key.second] = toNewState
        }
    }

    private fun calcTransition() {
        for(entry in gotoMap) {
            if(lr1ParserGenerator.terminalTokens.contains(entry.key.second) || true) {
                if(transitionMap.containsKey(entry.key)) {
                    throw Exception("LALR競合1 $entry")
                }
                transitionMap[entry.key] = TransitionData(
                    TransitionKind.SHIFT, entry.value, null
                )
            }
        }
        //printTransitionMap()
        for(entry in closureMap) {
            entry.key.filter { r -> r.reducible }.forEach {
                it.follow.forEach { token ->
                    if(transitionMap.containsKey(entry.value to token)) {
                        throw Exception("SLR競合2 $entry")
                    }
                    transitionMap[entry.value to token] = TransitionData(
                        TransitionKind.REDUCE, null, it.toRule()
                    )
                }
            }
            if(entry.key.any { r -> r.right == listOf(startSymbol) && r.follow.contains("$") && r.index == r.right.size }) {
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
        closureMap.forEach { (t, u) ->
            println("${u.padEnd(3)}: $t")
        }
    }

    fun printGotoMap() {
        println("Goto Table")
        print("   ")
        lr1ParserGenerator.terminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("  $  ")
        lr1ParserGenerator.nonTerminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("\n")
        for(c in closureMap.values) {
            print(c.padEnd(3))
            lr1ParserGenerator.terminalTokens.forEach { print(" ${gotoMap[c to it]?.padStart(3) ?: "   "} ") }
            print(" ${gotoMap[c to "$"]?.padStart(3) ?: "   "} ")
            lr1ParserGenerator.nonTerminalTokens.forEach { print(" ${gotoMap[c to it]?.padStart(3) ?: "   "} ") }
            print("\n")
        }
    }

    fun printTransitionMap() {
        println("Transition Table")
        print("   ")
        lr1ParserGenerator.terminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("  $  ")
        lr1ParserGenerator.nonTerminalTokens.forEach { print(" ${it.padStart(3)} ") }
        print("\n")
        for(c in closureMap.values) {
            print(c.padEnd(3))
            lr1ParserGenerator.terminalTokens.forEach { print(" ${transitionMap[c to it]?.toString()?.padStart(3) ?: "   "} ") }
            print(" ${transitionMap[c to "$"]?.toString()?.padStart(3) ?: "   "} ")
            lr1ParserGenerator.nonTerminalTokens.forEach { print(" ${transitionMap[c to it]?.toString()?.padStart(3) ?: "   "} ") }
            print("\n")
        }
    }

    data class LR1RuleCoreGroup(
        val cores: Set<LR1RuleCore>,
        val states: MutableList<String>
    )

    data class LR1RuleCore(
        val left: String,
        val right: List<String>,
        val index: Int,
        val reducible: Boolean
    )

    data class LALR1ProductionRuleData(
        val left: String,
        val right: List<String>,
        val index: Int,
        val reducible: Boolean,
        val follow: List<String>
    ) {
        fun shift(): LALR1ProductionRuleData {
            if(reducible) throw Exception("シフトできません $this")
            return LALR1ProductionRuleData(
                left, right, index+1,
                reducible = index+1 >= right.size,
                follow
            )
        }

        override fun toString(): String {
            return "$left->${right.toMutableList().apply { add(index,"・") }.joinToString("")} - $follow"
        }

        fun toRule() = LR1ParserGenerator.ProductionRuleData(
            left, right
        )
    }
}