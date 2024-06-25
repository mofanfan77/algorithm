package core.algorithm

import catalog.solution.AlgorithmSolution

interface AlgorithmInterface: SolutionProcessor {
    /**
     * 算法参数初始化
     */
    fun register()

    /**
    算法预处理逻辑：如初始解的生成
     */
    fun start()

    /**
    算法迭代逻辑
     */
    fun search()

    /**
    算法结束，记录算法指标和结果
     */
    fun end()

    /**
     * 获取结果
     */
    fun getResult(): AlgorithmSolution?

}