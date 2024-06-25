package catalog.solution

import analysis.Objective
import core.entity.PlanResource
import core.entity.Variable
import data.output.Output
import problem.BaseProblem

abstract class AlgorithmSolution {
    var variableList : MutableMap<Any, ArrayList<Variable>> = mutableMapOf()
    var objective = Objective()

    /**
     * 多层的目标值
     */
    fun getFitness(): Objective{
        return objective
    }

    fun getResult() : ArrayList<Variable>{
        var result : ArrayList<Variable> = arrayListOf()
        variableList.forEach { _, u ->
            result.addAll(u)
        }
        return result
    }

    fun saveSolution(input: Map<PlanResource, ArrayList<Variable>>){
        for ((k,v) in input) {
            this.variableList[k] = v
        }
    }

    fun log(){
        objective.log()
    }

    override fun toString(): String {
        return objective.toString()
    }

    abstract fun convertToOutput(problem: BaseProblem) : Output
}