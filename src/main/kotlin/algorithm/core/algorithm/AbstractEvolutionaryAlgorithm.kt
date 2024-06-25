package core.algorithm

import catalog.ea.Population
import catalog.solution.AlgorithmSolution
import config.AlgorithmConfig
import core.algorithm.AlgorithmInterface
import core.operator.initial.BaseGenerationRule
import core.operator.SelectionOperator
import core.operator.crossover.CrossOverOperator
import core.operator.mutation.MutationOpeator
import utility.enums.ParameterEnum
import kotlin.random.Random

abstract class AbstractEvolutionaryAlgorithm : AlgorithmInterface {

    var crossOverPool = mutableMapOf<Int, ArrayList<CrossOverOperator>>()
    var mutationPool = mutableMapOf<Int, ArrayList<MutationOpeator>>()
    var generatorPool = ArrayList<BaseGenerationRule>()
    var selectionPool = ArrayList<SelectionOperator>()
    var temperature = 0.0
    var realSolution: Array<Double>? = null
    var bestSolution: AlgorithmSolution? = null
    var localSolution: AlgorithmSolution? = null
    var curIteration = 1
    var bestIteration = 0
    var noImproveIter = 0
    var startCounter = 0L
    private var isStop = true
    lateinit var population: Population
    lateinit var parameter: AlgorithmConfig
    lateinit var random: Random

    override fun register() {
        if (parameter.getAsBoolean(ParameterEnum.Mode)){
            val seed = parameter.getAsInt(ParameterEnum.RandomSeed)
            random = Random(seed)
        }else{
            random = Random
        }
    }

    /**
     * 判定结束标准
     */
    abstract fun updateStopFlag()

    /**
     * 获取最终结果
     */
    override fun getResult(): AlgorithmSolution? {
        return null
    }

    /**
     * 结束运行
     */
    fun stop() {
        isStop = true
    }

    /**
     * 是否到达停止条件
     */
    fun checkStopCriteria(): Boolean {
        return isStop
    }

    /**
     * 开始运行
     */
    fun iterate() {
        isStop = false
    }
}