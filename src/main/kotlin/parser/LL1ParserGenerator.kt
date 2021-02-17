package parser

class LL1Parser(private val rules: List<ProductionRuleData>, private val startSymbol: String) {
    //開始記号はSとする
    //V do not contains $

    companion object {
        private const val EMPTY = "ε"
    }

    val terminalTokens = mutableListOf<String>()
    val nonTerminalTokens = mutableListOf<String>()
    val firstMap = mutableMapOf<String, MutableSet<String>>()
    val followMap = mutableMapOf<String, MutableSet<String>>()
    val directorMap = mutableMapOf<Pair<String, List<String>>, MutableSet<String>>()

    init {
        calcTokenKind()
        calcFirst()
        calcFollow()
        calcDirector()
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

    private fun addDirector(key: Pair<String,List<String>>, set: Set<String>) {
        directorMap[key]?.addAll(set) ?: kotlin.run {
            directorMap[key] = set.toMutableSet()
        }
    }

    private fun calcDirector() {
        println("CalcDirector")
        for(rule in rules) {
            val firstAlpha = getFirst(rule.right)
            if(!firstAlpha.contains(EMPTY)) {
                addDirector(rule.left to rule.right, firstAlpha)
            }
            else {
                addDirector(rule.left to rule.right, firstAlpha.minus(EMPTY))
                addDirector(rule.left to rule.right, followMap[rule.left] ?: emptySet())
            }
        }
        println("director")
        println("$directorMap")
    }

    private fun checkIsLL1Grammar(): Boolean {
        for(n in nonTerminalTokens) {
            val set = mutableSetOf<String>()
            for(entry in directorMap.filterKeys { k -> k.first == n }.entries) {
                if((entry.value intersect set).isNotEmpty()) {
                    return false
                }
                set.addAll(entry.value)
            }
        }
        return true
    }

    fun generateTable(): Map<Pair<String,String>, List<String>>? {
        if(!checkIsLL1Grammar()) return null

        val map = mutableMapOf<Pair<String,String>, List<String>>()

        for(entry in directorMap.toMap()) {
            for(t in entry.value) {
                map[entry.key.first to t] = entry.key.second
            }
        }
        return map
    }

    data class ProductionRuleData(
        val left: String,
        val right: List<String> //tokenized
    )
}
