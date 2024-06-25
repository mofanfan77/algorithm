package catalog.ea

import analysis.Objective
import analysis.evalutor.BaseEvaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Population {

    constructor(size: Int) {
        capacity = size
    }

    private var capacity = 100
    var pool = ArrayList<ChromoseBase>()

    fun addAll(solutions: Collection<ChromoseBase>) {
        pool.addAll(solutions)
    }

    fun reset() {
        pool.clear()
    }

    fun getCapacity(): Int {
        return capacity
    }

    fun renew(generation: List<ChromoseBase>) {
        this.reset()
        this.addAll(generation)
    }

    fun add(solution: ChromoseBase) {
        pool.add(solution)
    }

    fun getBestChromosome(): ChromoseBase {
        return pool.maxByOrNull { it.getFitness() }!!
    }

    /**
     * 记录各个层级最优值
     */
    fun recordRealSolution(): Array<Double>{
        val s = pool[0].getFitness().fitness.size
        val upper = Array<Double>(s){ -Double.MAX_VALUE }
        for (p in pool){
            val obj = p.getFitness()
            for (i in obj.fitness.indices) {
                upper[i] = maxOf(upper[i], obj.fitness[i])
            }
        }
        return upper
    }

    fun getBestChromosomeByTolerance(best: Array<Double>): ChromoseBase {
        val thresh = Array<Double>(best.size){0.0}
        val tol = pool[0].getFitness().builder.tolerance.values.toList()
        for (i in best.indices){
            if (best[i] < 0){
                thresh[i] = best[i] * (1+tol[i])
            }else{
                thresh[i] = best[i] * (1-tol[i])
            }
        }
        var bestV: Array<Double> = Array<Double>(best.size){-Double.MAX_VALUE}
        var bestTarget : ChromoseBase = pool[0]
        for (p in pool){
            var skip = false
            val obj = p.getFitness()
            for (j in (0 until  obj.fitness.size -1)){
                if (obj.fitness[j] < thresh[j]){
                    skip = true
                    break
                }
            }
            if (!skip){
                for (j in (0 until obj.fitness.size)){
                    val i = obj.fitness.size - 1 - j
                    if (obj.fitness[i] > bestTarget.getFitness().fitness[i]){ //更优
                        bestTarget = p
                        break
                    }else if (obj.fitness[i] < bestTarget.getFitness().fitness[i]){//更差
                        break
                    }
                }
            }
        }
        return bestTarget
    }

    fun updateChromosome(evaluator: BaseEvaluator) {
        partialUpdate(evaluator, this.pool)
    }

    operator fun get(index: Int): ChromoseBase {
        return pool[index]
    }

    operator fun set(index: Int, sol: ChromoseBase) {
        pool[index] = sol
    }

    companion object {
        fun createPopulation(capacity: Int): Population {
            return Population(capacity)
        }

        fun partialUpdate(evaluator: BaseEvaluator, pop: List<ChromoseBase>) {
//            val updateMap = splitBatch(pop)
//            updateMap.parallelStream().forEach { chrome ->
//                val currentThread = Thread.currentThread()
//                val systemTime = System.currentTimeMillis()
//                solverLog.debug("$currentThread start at : ${LocalDateTime.now()} with ${chrome.size} tasks")
//                for (idx in chrome.indices) {
//                    val chr = chrome[idx]
//                    evaluator.decoder(chr)
//                }
//                solverLog.debug("$currentThread end at : ${LocalDateTime.now()} with ${chrome.size} tasks, took ${System.currentTimeMillis() - systemTime} ms.")
//            }
            runBlocking {
                val scope = CoroutineScope(Dispatchers.Default)
                val jobs = pop.map { chromoseBase ->
                    scope.launch {
                        evaluator.decoder(chromoseBase)
                    }
                }
                jobs.forEach { it.join() }
            }
        }
    }
}