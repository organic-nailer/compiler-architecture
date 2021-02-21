package parser

class LR0Parser(
    private val gotoMap: Map<Pair<String, String>, String>,
    private val closureMap: Map<Set<LR0ParserGenerator.LRProductionRuleData>, String>
) {
    fun parse(input: List<String>): Node? {
        val reducibleMap = mutableMapOf<String,LR0ParserGenerator.LRProductionRuleData>() //Ixが還元を持っていれば還元先のtokenを返す
        for(entry in closureMap) {
            entry.key.firstOrNull { it.reducible }?.let {
                reducibleMap[entry.value] = it
            }
        }
        println("input: $input")
        val stack = ArrayDeque<Pair<String,String>>()// state,token
        val nodeStack = ArrayDeque<Node>()
        var parseIndex = 0
        //val stateStack = ArrayDeque<String>()
        var accepted = false
        stack.addFirst("I0" to "")
//        println("shiftf")
//        stack.addFirst(gotoMap[stack.first().first to input[parseIndex]]!! to input[parseIndex])
//        parseIndex++
//        nodeStack.addFirst(Node(stack.first().second, stack.first().second, mutableListOf()))
        while(parseIndex < input.size || stack.isNotEmpty()) {
            if(reducibleMap.containsKey(stack.first().first)) {
                val rule = reducibleMap[stack.first().first]!!
                println("reduce($rule): ${stack.first()} back to ${stack[1]}")
                val newNode = Node(rule.left, null, mutableListOf())
                for(t in rule.right.reversed()) {
                    if(stack.first().second == t) {
                        stack.removeFirst()
                        newNode.children.add(nodeStack.removeFirst())
                    }
                    else {
                        throw Exception("還元時エラー $rule, $stack, $t")
                    }
                }
                gotoMap[stack.first().first to rule.left]?.let {
                    stack.addFirst(it to rule.left)
                } ?: kotlin.run {
                    if(rule.right.last() != "$") {
                        throw Exception("還元shiftエラー $rule, $stack")
                    }
                }
                nodeStack.addFirst(newNode)
                if(rule.right.last() == "$") {
                    accepted = true //受理
                    break
                }
            }
            else if(gotoMap.containsKey(stack.first().first to input[parseIndex])) {
                println("shift(${stack.first()}): to ${gotoMap[stack.first().first to input[parseIndex]]!! to input[parseIndex]}")
                stack.addFirst(gotoMap[stack.first().first to input[parseIndex]]!! to input[parseIndex])
                parseIndex++
                nodeStack.addFirst(Node(stack.first().second, stack.first().second, mutableListOf()))
            }
            else {
                throw Exception("行き先が不明です at $stack, token=${input[parseIndex]}")
            }
            println("なう: ${input.toMutableList().apply { add(parseIndex, "・") }.joinToString("")}, $stack")
        }
        if(!accepted) {
            println("受理されませんでした ${nodeStack.first()}")
            return null
        }
        return nodeStack.first()
    }

//    fun parse(input: List<String>): Node? {
//        val reducibleMap = mutableMapOf<String,LR0ParserGenerator.LRProductionRuleData>() //Ixが還元を持っていれば還元先のtokenを返す
//        for(entry in closureMap) {
//            entry.key.firstOrNull { it.reducible }?.let {
//                reducibleMap[entry.value] = it
//            }
//        }
//        println("input: $input")
//        val stack = ArrayDeque<String>()
//        val nodeStack = ArrayDeque<Node>()
//        var parseIndex = 0
//        val stateStack = ArrayDeque<String>()
//        var accepted = false
//        stateStack.addFirst("I0")
//        stack.addFirst(input[parseIndex])
//        parseIndex++
//        while(parseIndex < input.size) {
//            if(gotoMap.containsKey(stateStack.first() to stack.first())) {
//                println("shift(${stack.first()}): ${stateStack.first()} to ${gotoMap[stateStack.first() to stack.first()]}")
//                stateStack.addFirst(gotoMap[stateStack.first() to stack.first()]!!)
//                nodeStack.addFirst(Node(stack.first(),stack.first(), mutableListOf()))
//                stack.addFirst(input[parseIndex])
//                parseIndex++
//            }
//            else if(reducibleMap.containsKey(stateStack.first())) {
//                val rule = reducibleMap[stateStack.first()]!!
//                println("reduce: ${stateStack.first()} back to ${stateStack[1]}")
//                val newNode = Node(rule.left,null, mutableListOf())
//                for(t in rule.right.reversed()) {
//                    if(stack.first() == t) {
//                        stack.removeFirst()
//                        newNode.children.add(nodeStack.removeFirst())
//                    }
//                    else {
//                        throw Exception("還元時エラー $rule, $stateStack, $t")
//                    }
//                }
//                stateStack.removeFirst()
//                stack.addFirst(rule.left)
//                nodeStack.addFirst(newNode)
//                if(rule.right.last() == "$") {
//                    accepted = true //受理
//                    break
//                }
//            }
//            else {
//                throw Exception("行き先が不明です at $stateStack, token=${input[parseIndex]}")
//            }
//        }
//        if(!accepted) {
//            println("受理されませんでした ${nodeStack.first()}")
//            return null
//        }
//        return nodeStack.first()
//    }

    data class Node(
        val kind: String,
        val value: String?,
        val children: MutableList<Node>
    ) {
        fun print(indent: String) {
            println(indent + this.kind)
            this.children.forEach { it.print("$indent  ") }
        }
    }
}