package utility.algorithm

import catalog.solution.AlgorithmSolution
import config.AlgorithmConfig
import core.algorithm.AbstractEvolutionaryAlgorithm
import org.example.algorithm.core.operator.Operator
import solver.solverLog

class AlgorithmListener(var algo: AbstractEvolutionaryAlgorithm) {

    var lastTimeTag = -1L
    var currTimeTag = -1L

    fun save(solution: AlgorithmSolution) {
        val logInfo = "第${algo.curIteration}次迭代，适应度为\n${solution.getFitness()}\n" +
                "当前最优结果为${algo.bestIteration}次迭代，适应度为\n${algo.bestSolution?.getFitness()}"
        solverLog.debug(logInfo)
        if (algo.curIteration % 10 == 1) {
            lastTimeTag = currTimeTag
            currTimeTag = System.currentTimeMillis()
            solverLog.info(logInfo)
            if (lastTimeTag > 0) {
                solverLog.info("${Thread.currentThread()} - Average calculation speed in the current generation is ${(currTimeTag - lastTimeTag) / 1000.0}s")
            }
        }
//    }
    }

    fun finish() {
        solverLog.info("最优结果出现在第" + algo.bestIteration.toString() + "次迭代，适应度为" + algo.bestSolution?.getFitness() + ",结果为: " + algo.bestSolution?.toString())
    }

    fun logParameter(parameter: AlgorithmConfig) {
        val logs = parameter.logOutSettings()
        solverLog.info(logs)
    }

    fun logOperatorStatistics() {
        logPool(algo.selectionPool)
        logPool(algo.crossOverPool)
        logPool(algo.mutationPool)
    }

    private fun <T> logPool(pool: Map<T, List<Operator>>) {
        for (p in pool) {
            val opt = p.value
            for (p2 in opt) {
                solverLog.info("=== ${p2.name} === 选择次数: ${p2.selectedTimes} === 累计分数: ${p2.score}")
            }
        }
    }

    private fun logPool(pool: List<Operator>) {
        for (p2 in pool) {
            solverLog.info("=== ${p2.name} === 选择次数: ${p2.selectedTimes} === 累计分数: ${p2.score}")
        }
    }

    fun <T> updatePool(pool: Map<T, List<Operator>>) {
        for (p1 in pool) {
            var total = 0.0
            for (p in p1.value){
                total += p.weight
            }
            for (p in p1.value){
                p.updateProbability(total)
            }
        }
    }

    fun updatePool(pool: List<Operator>) {
        var total = 0.0
        for (p in pool) {
            total += p.weight
        }
        for (p in pool) {
            p.updateProbability(total)
        }
    }
}