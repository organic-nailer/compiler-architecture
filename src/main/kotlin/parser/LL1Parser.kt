package parser

class LL1Parser(
    private val table: Map<Pair<String,String>, List<String>>,
    private val terminalTokens: List<String>,
    private val nonTerminalTokens: List<String>,
    private val startSymbol: String
    ) {
    fun parse(input: String): Node? {
        val stack = ArrayDeque<String>()
        var index = 0
        val nodeStack = ArrayDeque<Node>()
        nodeStack.addFirst(Node("root", null, mutableListOf()))
        stack.addFirst(startSymbol)
        while(stack.isNotEmpty()) {
            if(terminalTokens.contains(stack.first())) {
                if(input.indexOf(stack.first(), index) == index) {
                    val newNode = Node(stack.first(), input.substring(index, index + stack.first().length), mutableListOf())
                    nodeStack.first().children.add(newNode)
                    index += stack.first().length
                    stack.removeFirst()
                }
                else {
                    println("error1: index:$index .. stack=$stack")
                    return null
                }
            }
            else if(nonTerminalTokens.contains(stack.first())) {
                table[stack.first() to input[index].toString()]?.let {
                    if(it.size == 1 && it[0] == "ε") {
                        val a = stack.removeFirst()
                        nodeStack.first().children.add(Node(a, null, mutableListOf()))
                    }
                    else {
                        val a = stack.removeFirst()
                        stack.addFirst("$$")
                        it.asReversed().forEach { t -> stack.addFirst(t) }
                        val newNode = Node(a, null, mutableListOf())
                        nodeStack.first().children.add(newNode)
                        nodeStack.addFirst(newNode)
                    }
                } ?: kotlin.run {
                    println("error2: index:$index")
                    return null
                }
            }
            else if(stack.first() == "$$") {
                stack.removeFirst()
                nodeStack.removeFirst()
            }
        }
        if(input[index] != '$') {
            println("error3: index:$index")
            return null
        }
        return nodeStack.first()
    }

    data class Node(
        val kind: String,
        val value: String?,
        val children: MutableList<Node>
    )
}