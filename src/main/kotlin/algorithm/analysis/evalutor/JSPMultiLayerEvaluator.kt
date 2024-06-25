package analysis.evalutor

import catalog.ea.ChromoseBase
import config.BaseConfig
import core.entity.*
import problem.BasicSchedulingProblem
import utility.Util
import utility.annotation.Ignored
import utility.enums.ParameterEnum
import utility.exception.VariableSizeException
import utility.resourcePrefix
import utility.unicodeConnector
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class JSPMultiLayerEvaluator(problem: BasicSchedulingProblem) : JSPEvaluator(problem) {

    private val inboundRelation = problem.pieceStepNetwork!!.getInDegreeMap()
    private var multiPath = false
    private var layers = 1

    override fun getParameter(config: BaseConfig) {
        super.getParameter(config)
        multiPath = config.getAsBoolean(ParameterEnum.FlexiblePath)
        layers = config.getAsInt(ParameterEnum.NumberResource)
    }


    private fun dfs(new: PlanEntity, status: HashSet<Double>, result: ArrayList<Int>,
                    level3Entity: Map<Double, Int>,
                    level3Index: Map<Double, PlanEntity>) {
        // 若前置均完成,则将其加入列表
        if (new.level3Id in status) {
            return
        }
        val prevs = inboundRelation[new] ?: arrayListOf()
        val prevSorted = ArrayList<Pair<Double, Int>>() // 值 和 index
        for (dep in prevs) {
            if (dep.level3Id !in status) {
                // 未完成，则将自己加入优先队列
                prevSorted.add(Pair(dep.level3Id, level3Entity[dep.level3Id]!!))
            }
        }
        prevSorted.sortBy { it.second }
        for (after in prevSorted) {
            val cur = after.first
            val dep = level3Index[cur]!!
            dfs(dep, status, result, level3Entity, level3Index)
        }
        result.add(new.topoIndex)
        status.add(new.level3Id)
    }

    private fun pathInsert(newResult: List<PlanEntity>): LinkedList<Int> {
        val result = arrayListOf<Int>()
        val level3Entity = newResult.mapIndexed { index, planEntity ->
            planEntity.level3Id to index
        }.toMap()
        val level3Index = newResult.map { planEntity ->
            planEntity.level3Id to planEntity
        }.toMap()
        val status = HashSet<Double>()
        for (new in newResult) {
            if (new.level3Id !in status) {
                dfs(new, status, result, level3Entity, level3Index)
            }
        }
        return LinkedList(result)
    }

    /**
     * 基于输入序列，返回一个满足拓扑排序的列表
     */
    private fun decodePriority(priority: ArrayList<Int>): MutableMap<Int, LinkedList<Int>> {
        val result = mutableMapOf<Int, LinkedList<Int>>()
        var start = 0
        for ((k, v) in problem.pieceSteps.pieceCounter) {
            val newResult = priority.subList(start, start + v).toList().map { key ->
                problem.getEntityFromMask(Util.getUniqueMaskCodeOfEntity(k, key))
            }
            val afterResult = pathInsert(newResult)
            result[k] = afterResult
            start += v
        }
        return result
    }

    /**
     * 多重资源下解析MS序列
     */
    override fun decoderResource(chob: ChromoseBase): MutableMap<PlanEntity, Variable> {
        val seq = chob.getMultiResourceArray(layers)
        val initResult = mutableMapOf<PlanEntity, Variable>()
        var cumSum = 0
        val counter = problem.pieceSteps.pieceCounter
        for (entry in counter) {
            val t = entry.key
            val u = entry.value
            var lastEntityOnResource: EntityOnResource? = null
            var allowedList = arrayListOf<String>()
            for (i in 1..u) {
                val operationCode = "$t$unicodeConnector$i"
                val planEntity = problem.getEntityFromMask(operationCode)
                val machineIdx = seq[0][cumSum + i - 1]
                val secondResources = seq.subList(1, layers).map { it[cumSum + i - 1] }
                var resourceId = planEntity.getNodesByPrimaryGroup()[machineIdx]
                // 考虑禁用资源的情况
                lastEntityOnResource?.let {
                    allowedList = problem.getAllowedList(it)
                }
                if (allowedList.isNotEmpty()) {
                    val newMachineIdx = machineIdx % (allowedList.size)
                    resourceId = allowedList[newMachineIdx]
                }
                val resource = problem.getResource(resourceId)
                val eor = problem.getEntityOnResource(planEntity, resource)
                val target = Variable(planEntity, resource, eor)
                // 添加多重资源
                for (i1 in 1 until layers) {
                    val code = secondResources[i1-1]
                    val secResourceId = planEntity.getNodesByGroupIndex(i1)[code]
                    val secResource = problem.getResource(secResourceId)
                    target.addToSub(secResource)
                }
                initResult[planEntity] = target
                lastEntityOnResource = eor
            }
            cumSum += u
        }
        if (initResult.size != problem.pieceSteps.getPropertySize()) {
            throw VariableSizeException("解析后的任务数量异常")
        }
        return initResult
    }

    @Ignored
    override fun generateSequence(temp: ArrayList<Variable>, assignFlags: MutableMap<PlanEntity, Variable>) {
        val rewards = mutableMapOf<PlanResource, VariableSequence>()
        val subRewards = mutableMapOf<PlanResource, ArrayList<Variable>>()
        for (result in temp) {
            val resource = result.locate!!
            rewards.putIfAbsent(resource, VariableSequence(resource, problem))
            rewards[resource]!!.connect(result)
            val subResources = result.secondaryLocates
            for (subIdx in subResources.indices){
                val sub = subResources[subIdx]
                subRewards.putIfAbsent(sub, arrayListOf())
                result.addToSub("$resourcePrefix${sub.groupId}", subRewards[sub]?.lastOrNull())
                subRewards[sub]!!.add(result)
            }
        }
    }

    override fun decodeEntity(chob: ChromoseBase, assignFlags: MutableMap<PlanEntity, Variable>): ArrayList<Variable> {
        val seq = chob.getOSArray()
        val result = ArrayList<Variable>()
        if (multiPath) {
            val priority = chob.getPTArray().generateCodeArray()
            val groupPriority = decodePriority(priority)
            for (i in 0 until seq.getSize()) {
                val jobNr = seq[i]
                val opNr = groupPriority[jobNr]!!.poll()
                val maskCode = Util.getUniqueMaskCodeOfEntity(jobNr, opNr)
                val entity = problem.getEntityFromMask(maskCode)
                val variable = assignFlags[entity]!!
                variable.calcPriority = i + 1
                result.add(variable)
            }
        } else {
            val codeFreq = mutableMapOf<Int, Int>()
            for (i in 0 until seq.getSize()) {
                val jobNr = seq[i]
                val opNr = codeFreq.getOrDefault(jobNr, 1)
                val maskCode = Util.getUniqueMaskCodeOfEntity(jobNr, opNr)
                val entity = problem.getEntityFromMask(maskCode)
                val variable = assignFlags[entity]!!
                variable.calcPriority = i + 1
                result.add(variable)
                codeFreq[jobNr] = opNr + 1
            }
        }
        return result
    }
}
