package utility


/**
 * 用DFS实现环路检测
 */

class CircleDetector(private var vertices: Int) {
    private val adjList: MutableMap<Int, ArrayList<Int>> = mutableMapOf()

    fun getAdjList():MutableMap<Int, ArrayList<Int>>{
        return this.adjList
    }

    fun addEdge(src: Int, dest: Int): Boolean {
        adjList.computeIfAbsent(src) { arrayListOf() }.add(dest)

        val visited = BooleanArray(vertices + 1) { false }
        val recursionStack = BooleanArray(vertices + 1) { false }

        if (isCyclicUtil(src, visited, recursionStack)) {
//            println("Adding edge $src->$dest forms a cycle. Skipping.")
            adjList[src]?.removeLast()
            return false
        }
//        println("Added edge $src->$dest successfully.")
        return true
    }

    private fun isCyclicUtil(v: Int, visited: BooleanArray, recursionStack: BooleanArray): Boolean {
        if (!visited[v]) {
            visited[v] = true
            recursionStack[v] = true

            adjList[v]?.forEach { neighbor ->
                if (!visited[neighbor] && isCyclicUtil(neighbor, visited, recursionStack)) {
                    return true
                } else if (recursionStack[neighbor]) {
                    return true
                }
            }
        }
        recursionStack[v] = false
        return false
    }
}