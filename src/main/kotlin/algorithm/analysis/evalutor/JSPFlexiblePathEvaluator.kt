package analysis.evalutor

import catalog.ea.ChromoseBase
import core.entity.PlanEntity
import core.entity.PlanResource
import core.entity.Variable
import problem.BasicSchedulingProblem
import utility.Util
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class JSPFlexiblePathEvaluator(problem: BasicSchedulingProblem) : JSPEvaluator(problem) {

    private val inboundRelation = problem.pieceStepNetwork!!.getInDegreeMap()

    private fun dfs(new: PlanEntity, status: HashSet<Double>, result: ArrayList<Int>,
                    level3Entity: Map<Double, Int>,
                    level3Index: Map<Double, PlanEntity>){
        // 若前置均完成,则将其加入列表
        if (new.level3Id in status){
            return
        }
        val prevs = inboundRelation[new]?: arrayListOf()
        val prevSorted = ArrayList<Pair<Double, Int>>() // 值 和 index
        for (dep in prevs){
            if (dep.level3Id !in status){
                // 未完成，则将自己加入优先队列
                prevSorted.add(Pair(dep.level3Id, level3Entity[dep.level3Id]!!))
            }
        }
        prevSorted.sortBy { it.second }
        for (after in prevSorted){
            val cur = after.first
            val dep = level3Index[cur]!!
            dfs(dep, status, result, level3Entity, level3Index)
        }
        result.add(new.topoIndex)
        status.add(new.level3Id)
    }

    private fun pathInsert(newResult: List<PlanEntity>) : LinkedList<Int>{
        val result = arrayListOf<Int>()
        val level3Entity = newResult.mapIndexed { index, planEntity ->
            planEntity.level3Id to index
        }.toMap()
        val level3Index = newResult.map { planEntity ->
            planEntity.level3Id to planEntity
        }.toMap()
        val status = HashSet<Double>()
        for (new in newResult){
            if (new.level3Id !in status) {
                dfs(new, status, result, level3Entity, level3Index)
            }
        }
        return LinkedList(result)
    }

    /**
     * 基于输入序列，返回一个满足拓扑排序的列表
     */
    fun decodePriority(priority: ArrayList<Int>): MutableMap<Int, LinkedList<Int>>{
        val result = mutableMapOf<Int, LinkedList<Int>>()
        var start = 0
        for ((k,v) in problem.pieceSteps.pieceCounter){
            val newResult = priority.subList(start, start + v).toList().map {key->
                problem.getEntityFromMask(Util.getUniqueMaskCodeOfEntity(k, key))
            }
            val afterResult = pathInsert(newResult)
            result[k] = afterResult
            start += v
        }
        return result
    }

    override fun decodeEntity(chob: ChromoseBase, assignFlags: MutableMap<PlanEntity, Variable>): ArrayList<Variable>{
        val seq = chob.getOSArray()
        val priority = chob.getPTArray().generateCodeArray()
        val groupPriority = decodePriority(priority)
        val result = ArrayList<Variable>()
        for (i in 0 until seq.getSize()) {
            val jobNr = seq[i]
            val opNr = groupPriority[jobNr]!!.poll()
            val maskCode = Util.getUniqueMaskCodeOfEntity(jobNr, opNr)
            val entity = problem.getEntityFromMask(maskCode)
            val variable = assignFlags[entity]!!
            variable.calcPriority = i+1
            result.add(variable)
        }
        return result
    }
}