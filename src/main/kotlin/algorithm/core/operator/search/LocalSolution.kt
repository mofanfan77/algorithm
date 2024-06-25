package core.operator.search

import analysis.evalutor.BaseEvaluator
import catalog.ea.ChromoseBase
import catalog.ea.TwoPartitionChromosome
import catalog.ea.CodeSequence
import catalog.solution.FJSPSolution
import config.AlgorithmConfig
import core.algorithm.SinglePathHybridAlgorithm
import core.entity.PlanEntity
import core.entity.PlanResource
import core.entity.Variable
import problem.JobSchedulingProblem
import solver.solverLog
import utility.Util
import java.util.*
import kotlin.random.Random

class LocalSolution(var solution: TwoPartitionChromosome, var resource: String, var position: Int) {
    fun getKey(): String {
        return "${this.position}-${this.resource}"
    }
}


class TabuSearch {
    private val tabuListSize: Double = 30.0
    private var curT = 0.0
    private var maxT = 10.0
    private var tabuT: Double = -1.0
    private val tabuMap: MutableMap<String, Double> = mutableMapOf()
    private val tabuList: LinkedList<String> = LinkedList()
    private var problem: JobSchedulingProblem? = null
    fun run(
        algorithm: SinglePathHybridAlgorithm,
        evaluator: BaseEvaluator,
        problem: JobSchedulingProblem,
        parameter: AlgorithmConfig
    ) {
        val population = algorithm.population
        var found = "失败"
        this.problem = problem
        this.tabuT = parameter.maxTabuTenure
        this.maxT = (parameter.maxTSIteration * algorithm.curIteration * 1.0 / parameter.maxIteration).toInt().toDouble()
        solverLog.debug("Neighbor search algorithm start ...")
        for (index in population.pool.indices) {
            val solution = population.pool[index]
            val newSolution = this.search(solution, evaluator, problem)
            if (population[index].getFitness() <= newSolution.getFitness()) {
                found = "成功"
                population[index] = newSolution
            }
        }
        solverLog.debug("Neighbor search algorithm finished ..., 邻域搜索$found")
    }

    // 核心搜索流程
    private fun search(solution: ChromoseBase, evaluator: BaseEvaluator, problem: JobSchedulingProblem): ChromoseBase {
        tabuMap.clear()
        tabuList.clear()
        curT = 0.0
        var currentSolution = solution
        var bestSolution = solution
        while (!terminationCondition()) {
            val neighborhood = generateNeighborhood(currentSolution, problem)
            val candidate = findBestCandidate(neighborhood, evaluator)
            if (candidate != null) {
                currentSolution = candidate.solution
                if (currentSolution.getFitness() >= bestSolution.getFitness()) { // 优于当前最好解
                    bestSolution = currentSolution
                }
                updateTabuList(candidate)
            }
            curT += 1
        }
        return bestSolution
    }

    private fun terminationCondition(): Boolean {
        // 定义终止条件，例如达到一定迭代次数或时间限制
        return curT >= maxT
    }

    private fun swapMachine(
        solution: ChromoseBase,
        position: Int,
        ans: ArrayList<LocalSolution>,
        pair: Pair<PlanEntity, PlanResource>,
        entityIndex: Int
    ) {
        val swap2 = solution.getOSArray()
        val offspring = ArrayList<Int>(swap2.getSize())
        offspring.addAll(solution.getMSArray().generateCodeArray())
        val ub = solution.getMSArray().getUB(entityIndex)
//        for (k in 0 until ub) {
        if (ub > 1) { // 前提是有其他可选设备， 减小邻域范围
            var k = Random.nextInt(ub)
            if (k == offspring[entityIndex]) {
                k = Random.nextInt(ub)
            }
            offspring[entityIndex] = k
            val newSolution = LocalSolution(
                TwoPartitionChromosome(
                    solution.problem!!,
                    swap2.clone(),
                    CodeSequence(offspring, solution.getMSArray().generateUBArray())
                ),
                pair.second.resourceId,
                position
            )
            ans.add(newSolution)
        }
    }

    private fun swap(
        solution: TwoPartitionChromosome, position: Int, ans: ArrayList<LocalSolution>,
        before: Pair<PlanEntity, PlanResource>,
        after: Pair<PlanEntity, PlanResource>
    ) {
        val tempList = ArrayList<Variable>()
        val swap2 = solution.getMSArray()
        val offspring = ArrayList<Int>(swap2.getSize())
        tempList.addAll((solution.solution!! as FJSPSolution).getResult())
        tempList.sortedBy { it.getStartTime() }
        for (it in tempList) {
            when (it.block) {
                before.first -> {
                    val maskId = problem!!.getMaskOfEntity(after.first)
                    val maskJob = Util.parsePieceNumberFromMaskCode(maskId)
                    offspring.add(maskJob)
                }
                after.first -> {
                    val maskId = problem!!.getMaskOfEntity(before.first)
                    val maskJob = Util.parsePieceNumberFromMaskCode(maskId)
                    offspring.add(maskJob)
                }
                else -> {
                    val maskId = problem!!.getMaskOfEntity(it.block!!)
                    val maskJob = Util.parsePieceNumberFromMaskCode(maskId)
                    offspring.add(maskJob)
                }
            }
        }
        val newSolution = LocalSolution(
            TwoPartitionChromosome(solution.problem!!, CodeSequence(offspring), swap2.clone()),
            before.second.resourceId,
            -1
        )
        ans.add(newSolution)
    }

    private fun generateNeighborhood(solution: ChromoseBase, problem: JobSchedulingProblem): List<LocalSolution> {
        val criticalPath = problem.findCriticalPath(solution.solution!! as FJSPSolution)
        // 生成当前解的邻域解集合
        val ans = ArrayList<LocalSolution>()
        for (i in 1 until criticalPath.size) {
            // 判断是否是同一个资源上的不同工件的规划实体
//            if (criticalPath[i].first.jobNr != criticalPath[i - 1].first.jobNr && criticalPath[i].second == criticalPath[i - 1].second) {
//                swap(solution, i, ans, criticalPath[i], criticalPath[i - 1])
//            }
            // 把关键路径上的工件交换 达到优化的目的
            if (criticalPath[i].first.primaryId != criticalPath[i - 1].first.primaryId) {
                val index = problem.getIndexOfEntity(criticalPath[i - 1].first)
                swapMachine(solution, i, ans, criticalPath[i - 1], index)
            }
        }
        // 返回邻域解的列表
        return ans
    }

    private fun findBestCandidate(neighborhood: List<LocalSolution>, evaluator: BaseEvaluator): LocalSolution? {
        val candidate = ArrayList<LocalSolution>()
        // 在邻域解中寻找最优的候选解
        for (it in neighborhood) {
            if (!checkTabuCriteria(it)) {
                evaluator.decoder(it.solution)
                candidate.add(it)
            }
        }
        // 返回最优的候选解
        return candidate.maxByOrNull { it.solution.getFitness() }
    }

    private fun checkTabuCriteria(solution: LocalSolution): Boolean {
        var ans = true
        val key = solution.getKey()
        if (tabuMap.getOrDefault(key, curT) >= curT) {
            ans = false
        }
        return ans
    }

    private fun updateTabuList(solution: LocalSolution) {
        val tabuKey = solution.getKey()
        tabuMap[tabuKey] = curT + this.tabuT
        tabuList.add(tabuKey)
        if (tabuList.size > tabuListSize) {
            val key = tabuList.removeFirst()
            tabuMap.remove(key)
        }
    }
}