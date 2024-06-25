package catalog.solution

import analysis.Objective
import core.entity.PlanEntity
import core.entity.PlanResource
import core.entity.Variable
import data.BaseData
import data.dataobject.FJSPResultData
import data.output.Output
import problem.BaseProblem
import utility.Util
import kotlin.random.Random

class FJSPSolution : AlgorithmSolution {
    constructor()
    constructor(objective: Objective, arrResult: ArrayList<Variable>) : this() {
        val result = mutableMapOf<PlanResource, ArrayList<Variable>>()
        for (res in arrResult){
            result.putIfAbsent(res.locate!!, arrayListOf())
            result[res.locate!!]!!.add(res)
        }
        this.objective = objective
        this.saveSolution(result)
    }

    override fun convertToOutput(problem: BaseProblem): Output {
        val result = mutableMapOf<String, ArrayList<Variable>>()
        for ((_, variables) in this.variableList){
            for (variable in variables){
                val realMachine = variable.owner?.getId() ?: ""
                result.putIfAbsent(realMachine, arrayListOf())
                result[realMachine]!!.add(variable)
            }
        }
        val solutionMap = ArrayList<BaseData>()
        for (entry in result){
            val machine = entry.key
            val variables = entry.value
            for (variable in variables){
                if (variable.block != null) {
                    val operationId = variable.block!!.uniqueId
                    val preStart = variable.getPrefixStartTime()
                    val preEnd = variable.getPrefixEndTime()
                    val startTime = variable.getStartTime()
                    val endTime = variable.getEndTimeOnSameMachine()
                    val pojo = FJSPResultData(machine, operationId, preStart, preEnd, startTime, endTime)
                    solutionMap.add(pojo)
                }
            }
        }
        val output = Output()
        output.updateSolution(solutionMap)
        return output
    }

    fun getLatestResource(random: Random): String {
        val candidate = mutableListOf<String>()
        var lastTime = 0.0
        for ((_,us) in variableList){
            val u = us.lastOrNull()
            u?.let {
                if (u.getEndTime() >= lastTime) {
                    lastTime = u.getEndTime()
                    u.owner?.let { candidate.add(it.getId()) }
                }
            }
        }
        val ans = Util.randomSelect(candidate, random)
        return ans
    }

    fun getEntitiesFromResource(resource: String): List<PlanEntity> {
        val result = arrayListOf<PlanEntity>()
        for ((_,vs) in variableList){
            if (vs.firstOrNull()?.locate?.resourceId == resource){
                result.addAll(vs.map { it.block!! })
                break
            }
        }
        return result
    }
}