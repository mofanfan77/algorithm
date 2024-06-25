package analysis.evalutor

import analysis.Objective
import catalog.ea.ChromoseBase
import catalog.solution.FJSPSolution
import problem.BasicSchedulingProblem
import config.BaseConfig
import core.entity.*
import solver.solverLog
import utility.CircleDetector
import utility.Util
import utility.annotation.Ignored
import utility.enums.ParameterEnum
import utility.exception.VariableSizeException
import utility.unicodeConnector
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@Ignored
open class JSPEvaluator(var problem: BasicSchedulingProblem) : BaseEvaluator() {
    var loopRetry = 0.0
    var logMode = false
    override fun debugRun(chromosome: ChromoseBase) {
        logMode = true
        this.decoder(chromosome)
        logMode = false
        val solution = chromosome.solution!!
        solverLog.info("适应度为${solution.getFitness()},结果为${solution.toString()}")
    }

    override fun getParameter(config: BaseConfig) {
        loopRetry = config.getAsDouble(ParameterEnum.LoopRetry)
    }

    /**
     * 获取实体优先级
     */
    open fun getPriorityGroup(assignFlags: Map<PlanEntity, Variable>): Map<String, ArrayList<PlanEntity>> {
        val priorityGroups = assignFlags.values.sortedBy { it.calcPriority }.map { it.block!! }
            .groupBy { it.level1Id }
        val resourcePriorityGroup = mutableMapOf<String, ArrayList<PlanEntity>>()
        for (priorGroup in priorityGroups) {
            val enties = priorGroup.value
            for (entity in enties.reversed()) {
                val variable = assignFlags[entity]
                val newKey = variable?.locate?.resourceId + "$" + entity.level1Id
                resourcePriorityGroup.putIfAbsent(newKey, arrayListOf())
                resourcePriorityGroup[newKey]?.add(entity)
            }
        }
        return resourcePriorityGroup
    }

    /**
     * 任务解码
     */
    open fun decodeEntity(chob: ChromoseBase, assignFlags: MutableMap<PlanEntity, Variable>): ArrayList<Variable> {
        val seq = chob.getOSArray()
        val codeFreq = mutableMapOf<Int, Int>()
        val result = ArrayList<Variable>()
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
        return result
    }

    /**
     * 解码后处理（剔除环路，优先级约束）
     * 备注：同一资源上先把同一level2的任务完成
     */
    private fun improve(initialResult: ArrayList<Variable>, assignFlags: MutableMap<PlanEntity, Variable>): ArrayList<Variable> {
        val resourcePriorityGroup = getPriorityGroup(assignFlags)
        val levelGroup = problem.getPriorityOfLevel2()
        val assignResult = ArrayList<Variable>()
        val beforeSort = mutableMapOf<PlanResource, HashSet<Int>>()
        val entitySize = problem.pieceSteps.pieceCounter.keys
        val circleDetector = CircleDetector(entitySize.size)
        // 按照优先级提前加好
        for ((_, group) in levelGroup) {
            var idx = ""
            for (g in group) {
                if (idx != "") {
                    val src = problem.pieceSteps.relationMap[idx]
                    val desc = problem.pieceSteps.relationMap[g]
                    circleDetector.addEdge(src!!, desc!!)
                }
                idx = g
            }
        }
        val beforeVariables = mutableMapOf<String, Queue<Variable>>()
        val lastVarMap = mutableMapOf<PlanResource, Int>()
        for (init in initialResult) {
            val resource = init.locate!!
            lastVarMap.putIfAbsent(resource, -1)
            beforeSort.putIfAbsent(resource, HashSet())
            val entity = init.block!!
            val newKey = init.locate?.resourceId + "$" + entity.level1Id
            val priorityVariable = resourcePriorityGroup[newKey]?.removeLast()
            val newVariable = assignFlags[priorityVariable]!!
            val level2Id = problem.pieceSteps.relationMap[newVariable.block!!.primaryId]!!
            val key = "${resource.resourceId}*${level2Id}"
            beforeVariables.putIfAbsent(key, LinkedList())
            beforeSort[resource]?.add(problem.pieceSteps.relationMap[newVariable.block!!.primaryId]!!)
            beforeVariables[key]?.add(newVariable)
            val lastVar = lastVarMap[resource]!!
            if (lastVar != -1) { // 第一次循环
                circleDetector.addEdge(lastVar, level2Id)
            }
            lastVarMap[resource] = level2Id
        }
        val finalOrder = Util.topoSort(circleDetector.getAdjList())
        if (finalOrder.isNotEmpty()) {
            val tempResult = mutableMapOf<PlanResource, Queue<Variable>>()
            for (result in beforeSort) {
                for (order in finalOrder) {
                    if (order in result.value) {
                        val key = "${result.key.resourceId}*${order}"
                        tempResult.putIfAbsent(result.key, LinkedList())
                        tempResult[result.key]?.addAll(beforeVariables[key]!!)
                    }
                }
            }
            for (res in initialResult){
                val hold = res.locate!!
                assignResult.add(tempResult[hold]!!.poll())
            }
        } else { // finalOrder 为空, 不变
            assignResult.addAll(initialResult)
        }
        return assignResult
    }

    fun updateViaVice(assignFlags: MutableMap<PlanEntity, Variable>) {
        // 有副资源的限制
        if (problem.hasViceConstraint) {
            val viceQuantity = mutableMapOf<String, Double>()
            val viceLimit = problem.getViceResource()
            val viceSequence = mutableMapOf<String, Queue<Variable>>()
            val tempSort = assignFlags.values.sortedBy { it.getStartTime() }
            for (it in tempSort) {
                val viceMap = it.getViceResource()
                var resetTime = 0.0
                for ((vice, quantity) in viceMap) {
                    while (quantity + viceQuantity.getOrDefault(vice, 0.0) > viceLimit[vice]!!) { // 当前没有可用的副资源
                        val removeVariable = viceSequence[vice]!!.peek()
                        val releaseQuantity = removeVariable.getViceResourceQuantity(vice)
                        resetTime = maxOf(resetTime, removeVariable.getEndTime())
                        viceQuantity[vice] = viceQuantity[vice]!! - releaseQuantity
                    }
                    viceQuantity[vice] = viceQuantity.getOrDefault(vice, 0.0) + quantity
                    viceSequence.putIfAbsent(vice, ArrayDeque())
                    viceSequence[vice]!!.offer(it)
                }
                // 按照顺序
                it.updateStartTime(resetTime)
            }
        }
    }

    @Ignored
    open fun generateSequence(temp: ArrayList<Variable>, assignFlags: MutableMap<PlanEntity, Variable>) {
        val rewards = mutableMapOf<PlanResource, VariableSequence>()
        for (result in temp) {
            val resource = result.locate!!
            rewards.putIfAbsent(resource, VariableSequence(resource, problem))
            rewards[resource]!!.connect(result)
            val priors = problem.pieceStepNetwork!!.getPrevEntity(result.block!!)
            val subseqs = problem.getNextWIP(result.block!!).keys
            result.addAsPrior( priors.map { assignFlags[it]!! })
            result.addAsSubsequent( subseqs.map { assignFlags[it]!! } )
        }
    }

    /**
     * feat. 不再额外考虑副资源的场景
     */
    override fun decoder(chromosome: ChromoseBase) {
        // 获取机器列表
        val assignFlags = decoderResource(chromosome)
        val initialResult = decodeEntity(chromosome, assignFlags)
        val improveResult = improve(initialResult, assignFlags)
        // 根据结果生成
        this.generateSequence(improveResult, assignFlags)
        val engine = AttributeNetwork(problem, assignFlags, loopRetry)
        engine.initializeAttributes()
        engine.generateVariableMap()
        engine.run()
        val variables = assignFlags.values.toList()
        val objective = this.calculate(variables)
        val solution = FJSPSolution(objective, improveResult)
        problem.updateStatistics()
        chromosome.update(solution)
    }

    private fun calculate(rewards: List<Variable>): Objective {
        val obj = problem.getObjective()
        obj.calcObjective(rewards)
        return obj
    }

    /**
     * 解析MS序列
     */
    open fun decoderResource(chob: ChromoseBase): MutableMap<PlanEntity, Variable> {
        val seq = chob.getMSArray()
        val initResult = mutableMapOf<PlanEntity, Variable>()
        var cumSum = 0
        val counter = problem.pieceSteps.pieceCounter
        for (entry in counter) {
            val t = entry.key
            val u = entry.value
            var lastEntityOnResource: EntityOnResource? = null
            var allowedList = arrayListOf<String>()
            for (i in 1..u) {
                val operationCode = "$t${unicodeConnector}$i"
                val planEntity = problem.getEntityFromMask(operationCode)
                val machineIdx = seq[cumSum + i - 1]
                val availNodes = planEntity.getNodesByPrimaryGroup()
                var resourceId = availNodes[machineIdx]
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
                initResult[planEntity] = Variable(planEntity, resource, eor)
                lastEntityOnResource = eor
            }
            cumSum += u
        }
        if (initResult.size != problem.pieceSteps.getPropertySize()) {
            throw VariableSizeException("解析后的任务数量异常")
        }
        return initResult
    }
}