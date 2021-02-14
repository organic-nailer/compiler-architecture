package parser

class LL1Parser(val rules: List<ProductionRuleData>) {
    //開始記号はSとする
    //V do not contains $

    companion object {
        private const val EMPTY = 'ε'
    }

    val terminalTokens = mutableListOf<Char>()
    val nonTerminalTokens = mutableListOf<Char>()
    val firstMap = mutableMapOf<Char, MutableSet<Char>>()
    val followMap = mutableMapOf<Char, MutableSet<Char>>()
    val directorMap = mutableMapOf<Pair<Char,String>, MutableSet<Char>>()

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

    private fun getFirst(value: String): Set<Char> {
        if(value.isEmpty()) return setOf()
        if(terminalTokens.contains(value.first()) || value.first() == EMPTY) {
            return setOf(value.first())//{α} or {ε}
        }
        firstMap[value.first()]?.let {
            if(!it.contains(EMPTY) || value.length == 1) {
                return it //First(Y)
            }
            return it.minus(EMPTY).union(getFirst(value.substring(1))) //(First(Y)-{ε})∪First(α)
        }
        return emptySet()
    }

    private fun addFirst(key: Char, set: Set<Char>) {
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
            //println("tochu")
            //println("$firstMap")
        }
        println("first=")
        println("$firstMap")
    }

    //return true if set is updated
    private fun addFollow(key: Char, set: Set<Char>): Boolean {
        println("addFollow: current=${followMap[key]}, key=$key, set=$set")
        return followMap[key]?.addAll(set) ?: kotlin.run {
            followMap[key] = set.toMutableSet()
            return@run set.isNotEmpty()
        }
    }

    private fun calcFollow() {
        println("CalcFollow")
        followMap['S'] = mutableSetOf('$')
        var updated = true
        while(updated) {
            updated = false
            for(rule in rules) {
                for(i in rule.right.indices) {
                    if(nonTerminalTokens.contains(rule.right[i])) {
                        if(i >= rule.right.length - 1) {
                            val u = addFollow(rule.right[i], followMap[rule.left] ?: emptySet())
                            if(u) updated = true
                        }
                        else {
                            val firstBeta = getFirst(rule.right.substring(i+1))
                            println("First(${rule.right.substring(i+1)}) = $firstBeta")
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

    private fun addDirector(key: Pair<Char,String>, set: Set<Char>) {
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
}

data class ProductionRuleData(
    val left: Char,
    val right: String
)
