package parser

class LALR1Parser(
    private val transitionMap: Map<Pair<String, String>, LALR1ParserGenerator.TransitionData>
) {
    fun parse(input: List<String>): Node? {
        println("input: ${input.joinToString("")}")
        val stack = ArrayDeque<Pair<String,String>>()// state,token
        val nodeStack = ArrayDeque<Node>()
        var parseIndex = 0
        var accepted = false
        stack.addFirst("J0" to "")
        while(parseIndex < input.size || stack.isNotEmpty()) {
            val transition = transitionMap[stack.first().first to input[parseIndex]]
            when(transition?.kind) {
                LALR1ParserGenerator.TransitionKind.SHIFT -> {
                    stack.addFirst(transition.value!! to input[parseIndex])
                    parseIndex++
                    nodeStack.addFirst(Node(stack.first().second, stack.first().second, mutableListOf()))
                }
                LALR1ParserGenerator.TransitionKind.REDUCE -> {
                    val rule = transition.rule!!
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
                    transitionMap[stack.first().first to rule.left]?.value?.let {
                        stack.addFirst(it to rule.left)
                    } ?: kotlin.run {
                        throw Exception("還元shiftエラー $rule, $stack")
                    }
                    nodeStack.addFirst(newNode)
                }
                LALR1ParserGenerator.TransitionKind.ACCEPT -> {
                    accepted = true
                    break
                }
                else -> {
                    throw Exception("パースエラー: $stack, $parseIndex")
                }
            }
        }
        if(!accepted) {
            println("受理されませんでした ${nodeStack.first()}")
            return null
        }
        return nodeStack.first()
    }

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