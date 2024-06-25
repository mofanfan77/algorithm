package utility

import core.entity.EntityOnResource
import core.entity.PlanEntity
import core.entity.PlanResource
import utility.exception.NodeDependencyException
import utility.exception.UtilSortException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.random.Random


val unicodeConnector = "$"
val resourcePrefix = "SEC"

class Util {

    companion object {
        class Node<T>(source: T){
            var value = source
            var cost = 0.0

            constructor(source:T, distance: Double):this(source){
                this.cost = distance
            }
        }
        /**
         * 获取 desc 点的 入度计数
         */
        fun <T> getIndegreeMap(candidates: Map<T, ArrayList<T>>, containZero: Boolean = false): HashMap<T, Int> {
            val inDegrees = HashMap<T, Int>()
            if (containZero) {
                for (candidate in candidates) {
                    inDegrees.putIfAbsent(candidate.key, 0)
                }
            }
            for (candidate in candidates) {
                val afterSteps = candidate.value
                for (afterStep in afterSteps) {
                    inDegrees.putIfAbsent(afterStep, 0)
                    inDegrees[afterStep] = inDegrees[afterStep]!! + 1
                }
            }
            return inDegrees
        }

        /**
         * 考虑环的类拓扑
         * key 为destin点
         */
        fun <T> loopTopoSort(candidates: MutableMap<T, ArrayList<T>>): MutableList<T> {
            if (candidates.isEmpty()) {
                throw UtilSortException("参与排序的图中没有节点")
            }
            // 最终序列
            val sortedOrder = ArrayList<T>()
            // 当前路径
            val unvisited = ArrayList(candidates.keys)
            val visited = HashSet<T>()
            // core
            while (unvisited.isNotEmpty()) {
                val node = unvisited.removeLast()
                if (node !in visited) {
                    dfs(node, visited, candidates, sortedOrder)
                }
            }
            return sortedOrder
        }

        private fun <T> dfs(node: T, visited: HashSet<T>, candidates: MutableMap<T, ArrayList<T>>, sortedOrder: ArrayList<T>) {
            visited.add(node);
            for (neighbor in candidates[node] ?: arrayListOf()) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, visited, candidates, sortedOrder)
                }
            }
            sortedOrder.add(node);
        }

        /**
         * 加入随机因素的拓扑
         */
        fun <T> randomTopoSort(candidates: MutableMap<T, ArrayList<T>>, rand: Random): MutableList<T> {
            val inDegrees = getIndegreeMap(candidates)
            val queue = ArrayList<T>()
            for (key in candidates.keys) {
                if (!inDegrees.containsKey(key)) {
                    queue.add(key)
                }
            }
            // 最终序列
            val sortedOrder = ArrayList<T>()
            while (queue.isNotEmpty()) {
                val nodeIndex = rand.nextInt(queue.size)
                val node = queue.removeAt(nodeIndex)
                sortedOrder.add(node)
                val neighbors = candidates[node]
                if (neighbors?.isNotEmpty() == true) {
                    for (neighbor in neighbors) {
                        inDegrees[neighbor] = inDegrees[neighbor]!! - 1
                        if (inDegrees[neighbor] == 0) {
                            queue.add(neighbor)
                        }
                    }
                }
            }
            for (v in inDegrees.values) {
                if (v > 0) throw NodeDependencyException("传入的节点网络中存在循环依赖, ${inDegrees.filter { (_, v) -> v > 0 }.map { it.key }}")
            }
            return sortedOrder
        }

        /**
         * 自定义成本矩阵的最短拓扑排序算法
         */
        fun <T> minimumTopoSort(graph: Map<T, ArrayList<T>>, costFunction: (T, T) -> Double, rand: Random): ArrayList<T> {
            val degreeMap = getIndegreeMap(graph, true)
            val canBeVisited = ArrayList<T>()
            for ((key, v) in degreeMap) {
                if (v == 0) {
                    canBeVisited.add(key)
                }
            }
            var currentNode = randomSelect(canBeVisited, rand)
            val visited = arrayListOf<T>()
            visited.add(currentNode)
            for (neighbor in graph[currentNode] ?: emptyList()){
                degreeMap[neighbor] = degreeMap[neighbor]!! - 1
                if (degreeMap[neighbor] == 0){
                    canBeVisited.add(neighbor)
                }
            }
            var tempVisited = canBeVisited.toList()
            while (visited.size < degreeMap.size) {
                var bestV = Double.MAX_VALUE
                var bestNode : T? = null
                canBeVisited.clear()
                for (neighbor in tempVisited) {
                    if (neighbor in visited){
                        continue
                    }
                    canBeVisited.add(neighbor)
                    val newDistance = costFunction(currentNode, neighbor)
                    if (newDistance < bestV) {
                        bestV = newDistance
                        bestNode = neighbor
                    }
                }
                if (bestNode == null){
                    break
                }
                for (neighbor in graph[bestNode] ?: emptyList()){
                    degreeMap[neighbor] = degreeMap[neighbor]!! - 1
                    if (degreeMap[neighbor] == 0){
                        canBeVisited.add(neighbor)
                    }
                }
                visited.add(bestNode)
                currentNode = bestNode
                tempVisited = canBeVisited.toList()
            }
            return visited
        }

        fun <T> topoDFS(candidates: Map<T, ArrayList<T>>): MutableList<T> {
            val inDegrees = getIndegreeMap(candidates)
            val queue = arrayListOf<T>()
            for (key in candidates.keys) {
                if (!inDegrees.containsKey(key)) {
                    queue.add(key)
                }
            }
            // 最终序列
            queue.shuffle()
            val sortedOrder = ArrayList<T>()
            while (queue.isNotEmpty()) {
                val node = queue.removeLast()
                sortedOrder.add(node)
                val neighbors = candidates[node]
                if (neighbors?.isNotEmpty() == true) {
                    for (neighbor in neighbors) {
                        inDegrees[neighbor] = inDegrees[neighbor]!! - 1
                        if (inDegrees[neighbor] == 0) {
                            queue.add(neighbor)
                        }
                    }
                }
            }
            for (v in inDegrees.values) {
                if (v > 0) throw NodeDependencyException("传入的节点网络中存在循环依赖, ${inDegrees.filter { (_, v) -> v > 0 }.map { it.key }}")
            }
            return sortedOrder
        }

        fun <T> pickStartFromList(candidates: Map<T, ArrayList<T>>): MutableList<T> {
            val inDegrees = getIndegreeMap(candidates)
            val degree = ArrayList<T>()
            for (key in candidates.keys) {
                if (!inDegrees.containsKey(key)) {
                    degree.add(key)
                }
            }
            return degree
        }

        /**
         * key 为起点， 拓扑排序
         */
        fun <T> topoSort(candidates: Map<T, ArrayList<T>>): MutableList<T> {
            val inDegrees = getIndegreeMap(candidates)
            val queue: Queue<T> = LinkedList()
            for (key in candidates.keys) {
                if (!inDegrees.containsKey(key)) {
                    queue.offer(key)
                }
            }
            // 最终序列
            val sortedOrder = ArrayList<T>()
            while (queue.isNotEmpty()) {
                val node = queue.poll()
                sortedOrder.add(node)
                val neighbors = candidates[node]
                if (neighbors?.isNotEmpty() == true) {
                    for (neighbor in neighbors) {
                        inDegrees[neighbor] = inDegrees[neighbor]!! - 1
                        if (inDegrees[neighbor] == 0) {
                            queue.offer(neighbor)
                        }
                    }
                }
            }
            for (v in inDegrees.values) {
                if (v > 0) throw NodeDependencyException("传入的节点网络中存在循环依赖, ${inDegrees.filter { (_, v) -> v > 0 }.map { it.key }}")
            }
            return sortedOrder
        }

        fun <T> randomSelect(candidates: List<T>, rand : Random): T {
            val size = candidates.size
            val number = rand.nextInt(size)
            return candidates[number]
        }

        fun <T> randomMultipleSelect(candidates: List<T>, num: Int, rand: Random): List<T> {
            require(candidates.size >= num) { "随机序列长度不足" }
            val size = candidates.size
            val randomList = (0 until size).shuffled(rand)
            val ans = ArrayList<T>()
            val slice = randomList.subList(0, num)
            for (it in slice) {
                ans.add(candidates[randomList[it]])
            }
            return ans
        }

        fun getUniqueCodeOfNr(jobNr: String, operationNr: Double): String {
            return "${jobNr}${unicodeConnector}${operationNr.toInt()}"
        }

        fun getUniqueCodeOfEntity(ps: PlanEntity): String {
            if (ps.level3Id > 0) {
                return "${ps.primaryId}${unicodeConnector}${ps.level3Id.toInt()}"
            } else {
                return ps.primaryId
            }
        }

        fun parsePieceNumberFromMaskCode(maskCode: String): Int {
            return maskCode.split(unicodeConnector)[0].toInt()
        }

        fun parsePieceNrFromRealCode(maskCode: String): String {
            return maskCode.split(unicodeConnector)[0]
        }

        fun parseSeqNumberFromMaskCode(maskCode: String): Int {
            return maskCode.split(unicodeConnector)[1].toInt()
        }

        fun getUniqueCodeOfResource(ps: PlanResource): String {
            return ps.resourceId
        }

        fun getUniqueCodeOfPieceStepOnResource(ps: EntityOnResource): String {
            return ps.resourceId + unicodeConnector + ps.entityId
        }

        fun getUniqueCodeOfPieceStepOnResource(ps: PlanEntity, re: PlanResource): String {
            return re.resourceId + unicodeConnector + getUniqueCodeOfEntity(ps)
        }

        fun getUniqueMaskCodeOfEntity(jobNr: Int, opNr: Int): String {
            return "${jobNr}${unicodeConnector}${opNr}"
        }

        fun <T> splitBatch(data: List<T>): List<List<T>> {
            val cpuCnt = Runtime.getRuntime().availableProcessors()
            val batchSize = data.size / cpuCnt + 1
            return data.chunked(batchSize)
        }

        fun <T> addToGroup(group: MutableMap<String, ArrayList<T>>, obj: T, string: String){
            group.putIfAbsent(string, arrayListOf())
            group[string]!!.add(obj)
        }
    }

}