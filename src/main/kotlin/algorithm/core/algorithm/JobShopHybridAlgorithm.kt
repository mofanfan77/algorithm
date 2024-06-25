package core.algorithm

import catalog.ea.ChromoseBase
import problem.JobSchedulingProblem
import utility.enums.ParameterEnum

abstract class JobShopHybridAlgorithm : HybridAlgorithm() {
    lateinit var problem: JobSchedulingProblem

    override fun generate(number: Double): ArrayList<ChromoseBase> {
        val ans = ArrayList<ChromoseBase>()
        for (i in 1..number.toInt()) {
            val operator = selectOperator(generatorPool)
            val toAdd = operator.operate(this.problem)
            ans.add(toAdd)
        }
        return ans
    }

    override fun addInitSolution() {
        val flag = parameter.getAsBoolean(ParameterEnum.FlexiblePath)
        val children = problem.getInitialSolution(flag)
        children?.let { this.population.add(children) }
    }

    override fun initialEvaluate() {
        val flag = parameter.getAsBoolean(ParameterEnum.FlexiblePath)
        val children = problem.getInitialSolution(flag)!!
        evaluator.debugRun(children)
    }
}