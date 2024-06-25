package core.algorithm

import analysis.evalutor.BaseEvaluator
import catalog.ea.ChromoseBase
import catalog.ea.Population
import catalog.solution.AlgorithmSolution
import catalog.solution.ExploredSolution
import org.example.algorithm.core.operator.Operator
import core.operator.search.TabuSearch
import solver.solverLog
import utility.algorithm.AlgorithmListener
import kotlin.math.exp

abstract class HybridAlgorithm : AbstractEvolutionaryAlgorithm() {
    lateinit var evaluator: BaseEvaluator
    var searcher = TabuSearch()
    var selectedOperators = ArrayList<org.example.algorithm.core.operator.Operator>()
    var listener = AlgorithmListener(this)

    // 算子策略
    /**
     * 初始化算子库
     */
    abstract fun establishPool()
    abstract fun crossover()
    abstract fun mutation()
    open fun localSearch() {}

    fun selection() {
        if (curIteration - noImproveIter >= parameter.maxStagIteration) { // 重新选择子代
            val popSize = this.parameter.popSize
            this.population.pool.sortByDescending { it.getFitness() }
            val offspringSize = (popSize * parameter.reproductionRatio).toInt()
            val children = ArrayList<ChromoseBase>()
            for (i in 1..offspringSize) {
                children.add(this.population.pool[i])
            }
            val newChildren = this.generate(popSize - offspringSize)
            children.addAll(newChildren)
            this.population.reset()
            this.population.addAll(children)
            this.calcFitness()
            noImproveIter = curIteration
        } else {
            val operator = selectOperator(selectionPool)
            operator.operate(this.population)
            this.selectedOperators.add(operator)
        }
    }

    abstract fun addInitSolution()
    /**
     * 添加初始解
     */
//    fun addInitSolution() {
//        val children = problem.getInitialSolution(parameter.flexiblePath)
//        children?.let { this.population.add(children) }
//    }

    /**
     * 计算种群适应度
     */
    fun calcFitness() {
        this.population.updateChromosome(evaluator)
    }

    /**
     * 初始化起始解
     */
    private fun initialBestSol() {
        val bestSol = this.population.getBestChromosome().solution!!
        this.bestSolution = bestSol
        this.realSolution = this.population.recordRealSolution()
        this.localSolution = bestSol
    }

    /**
     * 进化算法：初始化种群
     */
    private fun initialPop() {
        this.population = Population.createPopulation(parameter.popSize.toInt())
        val chidlren = this.generate(parameter.popSize * 2)
        this.population.addAll(chidlren)
    }

    /**
     * 生成类算子
     */
    abstract fun generate(number: Double): ArrayList<ChromoseBase>


    /**
     * 前处理
     */
    override fun start() {
        startCounter = System.currentTimeMillis()
        this.establishPool()
        this.updateOperator()
        this.initialPop()
        this.addInitSolution()
        this.calcFitness()
        this.initialBestSol()
        this.iterate()
        this.initialTemperature()
    }

    /**
     * 初始化温度
     */
    private fun initialTemperature() {
        this.temperature = this.parameter.initialTemperature
    }

    /**
     * 更新温度，用于判断是否接受差解
     */
    private fun updateTemperature() {
        this.temperature *= this.parameter.temperatureUpdateRatio
    }

    override fun <T : ExploredSolution> evaluate(exp: T) {
        if (exp is ChromoseBase) {
            evaluator.decoder(exp)
        }
    }

//    override fun initialEvaluate() {
//        val children = problem.getInitialSolution(parameter.flexiblePath)!!
//        evaluator.debugRun(children)
//    }

    /**
     * 搜索
     */
    override fun search() {
        while (!checkStopCriteria()) {
            selectedOperators.clear()
            selection()
            crossover()
            mutation()
            updateResult()
            updateOperator()
            localSearch()
            curIteration += 1
            updateTemperature()
            updateStopFlag()
        }
    }

    /**
     * 后处理
     */
    override fun end() {
        listener.logParameter(parameter)
        listener.logOperatorStatistics()
        listener.finish()
    }

    /**
     * 获取最终结果
     */
    override fun getResult(): AlgorithmSolution? {
        return this.bestSolution
    }

    /**
     * 判断迭代结束标准
     */
    override fun updateStopFlag() {
        val counter = System.currentTimeMillis() - startCounter
        if (curIteration > parameter.maxIteration) {
            solverLog.info("Reached max iteration limit ... ${parameter.maxIteration}")
            this.stop()
        } else if (counter > parameter.solveLimit * 1000) {
            solverLog.info("Reached max time limit ... ${parameter.solveLimit}s")
            this.stop()
        }
    }

    /**
     * 记录迭代数据
     */
    private fun sendLogToManager(solution: AlgorithmSolution) {
        listener.save(solution)
    }

    /**
     * 单轮迭代结果更新
     */
    private fun updateResult() {
        this.realSolution = this.population.recordRealSolution()
        val bestSol = this.population.getBestChromosomeByTolerance(this.realSolution!!).solution!!
        this.sendLogToManager(bestSol)
        if (this.localSolution == null || bestSol.getFitness().outperform(this.localSolution!!.getFitness(), this.realSolution!!)) {
            this.localSolution = bestSol
            if (this.bestSolution == null || bestSol.getFitness().outperform(this.bestSolution!!.getFitness(), this.realSolution!!)) {
                bestIteration = curIteration
                noImproveIter = curIteration
                this.bestSolution = bestSol
                // 更新算子分数
                for (opt in selectedOperators) {
                    opt.bonusAction(parameter.improveScore, parameter.weightAlpha)
                }
            } else {
                // 更新算子分数
                for (opt in selectedOperators) {
                    opt.bonusAction(parameter.bonusScore, parameter.weightAlpha)
                }
            }
        } else {
            val acceptThresh = getAcceptanceThresh(bestSol, this.localSolution!!)
            val acceptChance = random.nextDouble()
            if (acceptChance < acceptThresh) {
                this.localSolution = bestSol
            }
            for (opt in selectedOperators) {
                opt.bonusAction(parameter.penaltyScore, parameter.weightAlpha)
            }
        }
    }

    /**
     * 计算接受差解的阈值
     */
    private fun getAcceptanceThresh(sol: AlgorithmSolution, local: AlgorithmSolution): Double { // 退火框架，接受差解
        val deltaE = local.getFitness() - sol.getFitness() // 为正数
        return exp(-deltaE / temperature)
    }

    private fun updateOperator() {
        // 更新算子权重
        listener.updatePool(crossOverPool)
        listener.updatePool(mutationPool)
        listener.updatePool(selectionPool)
        listener.updatePool(generatorPool)
    }

    /**
     * 从算子库中根据概率随机选择算子
     */
    fun <T : org.example.algorithm.core.operator.Operator> selectOperator(pool: ArrayList<T>): T {
        val rand = random.nextDouble()
        var cum = 0.0
        require(pool.size > 0) { "此类型算子库未初始化" }
        var ans: T = pool[0]
        for (i in 0 until pool.size) {
            if (rand < cum + pool[i].probability) {
                ans = pool[i]
                break
            }
            cum += pool[i].probability
        }
        return ans
    }
}