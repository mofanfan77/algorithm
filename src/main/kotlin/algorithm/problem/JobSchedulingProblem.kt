package problem

import catalog.component.EntityNetwork
import catalog.component.JSPRuleManager
import catalog.ea.ChromoseBase
import catalog.ea.TwoPartitionChromosome
import catalog.ea.CodeSequence
import catalog.ea.ThreePartitionChromosome
import catalog.solution.FJSPInitSolution
import catalog.solution.FJSPSolution
import core.constraint.CleanRuleConstraint
import core.constraint.Constraint
import core.constraint.PriorConstraint
import core.entity.*
import data.placeholder.EntityData
import data.placeholder.PieceStepOnResourceData
import data.placeholder.ResourceData
import data.snapshot.FJSPSnapShot
import utility.Util
import utility.exception.InputDataNotFoundException
import utility.exception.InputDataOverFlowError

abstract class JobSchedulingProblem : BaseProblem() {
    var resources = ResourceData()
    var resourceLevels = 0
    var pieceSteps = EntityData()
    var pieceStepOnResources = PieceStepOnResourceData()
    var rules = JSPRuleManager()
    var pieceStepNetwork: EntityNetwork? = null
    var hasWIPConstraint = false
    var hasViceConstraint = false
    private var prevWIPConnection = mutableMapOf<String, MutableMap<String, Double>>()
    private var nextWIPConnection = mutableMapOf<String, MutableMap<String, Double>>()
    private var priorityEntity: MutableMap<String, List<PlanEntity>> = mutableMapOf<String, List<PlanEntity>>()
    private var priorityLevel: MutableMap<String, ArrayList<String>> = mutableMapOf()
    private var initSol: ArrayList<FJSPInitSolution> = arrayListOf()

    override fun dispose() {
        resources.reset()
        pieceSteps.reset()
        pieceStepOnResources.reset()
        rules.reset()
        pieceStepNetwork = null
        prevWIPConnection.clear()
        nextWIPConnection.clear()
        priorityEntity.clear()
        priorityLevel.clear()
        initSol.clear()
    }

    /**
     * 生成工件网络图
     */
    fun createNetwork(): JobSchedulingProblem {
        this.pieceStepNetwork = EntityNetwork()
        return this
    }

    /**
     * ****自动计算变量网络****
     */
    private fun addJobConnection(previous: String, after: String) {
        val prevEnt = this.getEntity(previous)
        val afterEnt = this.getEntity(after)
        this.pieceStepNetwork?.connect(prevEnt, afterEnt)
    }

    /**
     * 添加工件WIP网络依赖
     */
    private fun addWIPConnection(prev: String, after: String, duration: Double) {
        hasWIPConstraint = true
        prevWIPConnection.putIfAbsent(after, mutableMapOf())
        prevWIPConnection[after]!![prev] = duration
        nextWIPConnection.putIfAbsent(prev, mutableMapOf())
        nextWIPConnection[prev]!![after] = duration
    }

    fun readInitialSolution(inputs: List<FJSPInitSolution>) {
        for (input in inputs) {
            if (!input.isInitialized()) {
                throw IllegalStateException("some FJSPInitSolution instances are not fully initialized.")
            }
        }
        val res = ArrayList<FJSPInitSolution>()
        res.addAll(inputs)
        this.initSol = res
    }

    private fun createResourceSequence(): ArrayList<Int> {
        val machine = ArrayList<Int>()
        for (i in 1..this.pieceSteps.getPropertySize()) {
            machine.add(0)
        }
        for (entry in initSol) {
            val resourceId = entry.resource.resourceId
            val entity = entry.entity
            val indexOfEntity = getIndexOfEntity(entity)
            var idx = 0
            var ans = -1
            for (mh in entity.getNodesByPrimaryGroup()) {
                if (mh == resourceId) {
                    ans = idx
                    continue
                } else {
                    idx += 1
                }
            }
            machine[indexOfEntity] = ans
        }
        return machine
    }

    private fun createPrioritySequence(): ArrayList<Int> {
        val jobPoolSize = this.pieceStepNetwork!!.topoOrderByJob.size
        val result = ArrayList<ArrayList<Int>>(jobPoolSize)
        val finalResult = ArrayList<Int>(this.initSol.size)
        for ((_, v) in this.pieceStepNetwork!!.topoOrderByJob) {
            result.add(ArrayList<Int>(v.size))
        }
        for (entry in this.initSol) {
            val entity = entry.entity
            val jobIndex = this.pieceSteps.relationMap[entity.primaryId]!!
            result[jobIndex-1].add(entity.topoIndex)
        }
        for (vs in result){
            finalResult.addAll(vs)
        }
        return finalResult
    }

    private fun createEntitySequence(): ArrayList<Int> {
        val operation = ArrayList<Int>()
        val resources = mutableMapOf<PlanResource, ArrayList<PlanEntity>>()
        val connection = mutableMapOf<PlanEntity, PlanEntity>()
        val addSet = ArrayList<String>()
        for (entry in this.initSol) {
            val res = entry.resource
            val unique = entry.entity
            resources.putIfAbsent(res, arrayListOf())
            resources[res]?.lastOrNull()?.let { las ->
                connection[unique] = las
            }
            resources[res]?.add(unique)
        }
        for (entry in this.initSol) {
            val entity = entry.entity
            addOperation(entity, connection, addSet, operation)
        }
        return operation
    }

    private fun addOperation(
        entity: PlanEntity,
        connection: MutableMap<PlanEntity, PlanEntity>,
        addSet: ArrayList<String>,
        operation: ArrayList<Int>) {
        if (entity.uniqueId !in addSet) {
            if (entity.uniqueId in addSet) {
                throw InputDataOverFlowError("加入实体时出现循环网络${entity.uniqueId}, \n ${addSet.joinToString(" ;\n")}")
            }
            addSet.add(entity.uniqueId)
            val entityCode = this.pieceSteps.getMaskCode(entity.uniqueId)
            val prevEntity = this.getPreviousOperations(entity)
            if (prevEntity.isNotEmpty()) {
                for (ent in prevEntity) {
                    addOperation(ent, connection, addSet, operation)
                }
            } else {
                val jobNr = Util.parsePieceNumberFromMaskCode(entityCode!!)
                val seqNr = Util.parseSeqNumberFromMaskCode(entityCode)
                if (seqNr > 1) {
                    val newCode = Util.getUniqueMaskCodeOfEntity(jobNr, seqNr - 1)
                    val ent = this.getEntityFromMask(newCode)
                    addOperation(ent, connection, addSet, operation)
                }
            }
            val priorEntity = connection[entity]
            priorEntity?.let {
                addOperation(it, connection, addSet, operation)
            }
            var jobNr = Util.parsePieceNumberFromMaskCode(entityCode!!)
            operation.add(jobNr)
        }
    }

    /**
     * 根据初始解
     */
    fun getInitialSolution(flexiblePath: Boolean): ChromoseBase? {
        var chrom: ChromoseBase? = null
        val machine = createResourceSequence()
        val codeSequence2 = CodeSequence(machine)
        if (this.initSol.isNotEmpty()) {
            if (!flexiblePath) {
                val operation = createEntitySequence()
                val codeSequence1 = CodeSequence(operation)
                chrom = TwoPartitionChromosome(this, codeSequence1, codeSequence2)
            } else {
                val operation = createEntitySequence()
                val priority = createPrioritySequence()
                val codeSequence1 = CodeSequence(operation)
                val codeSequence3 = CodeSequence(priority)
                chrom = ThreePartitionChromosome(this, codeSequence1, codeSequence2, codeSequence3)
            }
        }
        return chrom
    }

    // 后序wip工件
    fun getNextWIP(planEntity: PlanEntity): MutableMap<PlanEntity, Double> {
        val ans = mutableMapOf<PlanEntity, Double>()
        val nextWIPs = nextWIPConnection[planEntity.uniqueId]
        if (nextWIPs != null) {
            for ((after, duration) in nextWIPs) {
                val obj = this.getEntity(after)
                ans[obj] = duration
            }
        }
        return ans
    }

    /**
     * 添加资源方法
     */
    fun addResources(resources: Collection<PlanResource>) {
        var index = 0
        val resourceLevel = HashSet<Int>()
        for (resource in resources) {
            this.resources.addEntity(resource)
            this.resources.addMask("${index + 1}", resource)
            index++
            resourceLevel.add(resource.groupId)
        }
        this.resourceLevels = resourceLevel.size
    }

    /**
     * 添加实体方法
     */
    fun addEntities(pieceSteps: Collection<PlanEntity>) {
        for (ps in pieceSteps) {
            ps.calcUniqueId()
            ps as JobEntity
            this.pieceSteps.addEntity(ps)
            this.pieceStepNetwork?.add(ps)
        }
        // 完善节点网络
        for (ps in pieceSteps) {
            ps as JobEntity
            if (ps.wipFlag) { // 如果是WIP，加入工件网络
                for (nextOp in ps.nextOpeartion) {
                    val nextId = Util.getUniqueCodeOfNr(ps.primaryId, nextOp)
                    this.addWIPConnection(ps.uniqueId, nextId, ps.wipDuration)
                }
            }
            if (ps.prevOpeartion.isNotEmpty()) { // 存在前序工件
                for (prevOp in ps.prevOpeartion) {
                    val prevId = Util.getUniqueCodeOfNr(ps.primaryId, prevOp)
                    this.addJobConnection(prevId, ps.uniqueId)
                }
            }
        }
    }

    /**
     * 添加资源实体交互
     */
    fun addEntityOnResources(pieceStepOnResources: Collection<EntityOnResource>) {
        for (enor in pieceStepOnResources) {
            enor.calcEntityId()
            val ent = this.getEntity(enor.entityId)
            val res = this.getResource(enor.resourceId)
//            if (res.groupId != 0){
//                ent.availableNodes.add(enor.resourceId)
//            }
            ent.addNodes(res)
            this.pieceStepOnResources.addEntity(enor)
        }
    }

    /**
     * 添加日历数据
     */
    fun addCalendars(calendars: Collection<PlanCalendar>) {
        for (calendar in calendars) {
            val res = this.getResource(calendar.resourceId)
            res.addCalendar(calendar, this.getTime())
        }
    }

    /**
     * 用规划实体 获取 算法代码
     */
    fun getMaskOfEntity(value: PlanEntity): String {
        return this.pieceSteps.getReversedMaskMap()[this.pieceSteps.getEntityId(value)]!!
    }

    /**
     * 用算法代码 获取 规划实体
     */
    fun getEntityFromMask(maskCode: String): PlanEntity {
        return this.pieceSteps.getPropertyMap()[this.pieceSteps.getMaskMap()[maskCode]!!]!!
    }

//    /**
//     * 获取实体代码的优先级（每个实体分组，组内有优先级）
//     */
//    fun getPriorityOfEntity(): Map<String, List<PlanEntity>> {
//        if (this.priorityEntity.isEmpty()) {
//            this.priorityEntity = this.pieceSteps.getPropertyMap().values.sortedWith(
//                    compareBy(
//                            PlanEntity::level3Id,
//                            PlanEntity::level2Id
//                    )
//            ).groupBy { it.level1Id }
//        }
//        return this.priorityEntity
//    }

    /**
     * 获取每个分组内level2的优先级
     */
    fun getPriorityOfLevel2(): Map<String, List<String>> {
        val ans = mutableMapOf<String, ArrayList<String>>()
        if (this.priorityLevel.isEmpty()) {
            val levelGroup = this.pieceSteps.getPropertyMap().values.sortedBy { it.primaryId }.groupBy { it.level1Id }
            for ((level1, group) in levelGroup) {
                ans.putIfAbsent(level1, arrayListOf())
                ans[level1]?.addAll(group.map { it.primaryId }.toList().toSet().toList())
            }
            this.priorityLevel = ans
        }
        return this.priorityLevel
    }

    /**
     * 用实体代码 获取 规划实体
     */
    fun getEntity(maskCode: String): PlanEntity {
        val obj = this.pieceSteps.getPropertyMap()[maskCode]
            ?: throw InputDataNotFoundException("传入的entity $maskCode 无法找到对应的实体")
        return obj
    }

    fun getResource(maskCode: String): PlanResource {
        return this.resources.getPropertyMap()[maskCode]!!
    }

    fun getEntityOnResource(ps: PlanEntity, re: PlanResource): EntityOnResource {
        val psorKey = Util.getUniqueCodeOfPieceStepOnResource(ps, re)
        return this.pieceStepOnResources.getPropertyMap()[psorKey]!!
    }

    fun getIndexOfEntity(entity: PlanEntity): Int {
        var ans = 0
        for ((job, num) in this.pieceSteps.pieceCounter) {
            val maskId = getMaskOfEntity(entity)
            val maskJob = Util.parsePieceNumberFromMaskCode(maskId)
            if (job < maskJob) {
                ans += num
            } else if (job == maskJob) {
                // 不能直接用level3Id
//                ans += entity.level3Id.toInt()
                // 取拓扑值
                val groupId = Util.parsePieceNrFromRealCode(entity.uniqueId)
                for (name in (pieceStepNetwork?.topoOrderByJob!![groupId] ?: arrayListOf())) {
                    ans++
                    if (name == entity) {
                        break
                    }
                }
            }
        }
        return ans - 1
    }

    fun getAllowedList(eor: EntityOnResource): ArrayList<String> {
        return this.rules.getAllowedList(eor)
    }

    // 添加计算时间约束
    fun addPriorConstraint(name: String, targetAttr: Attributes, sourceAttr: Attributes) {
        val constr = PriorConstraint(name, targetAttr, sourceAttr)
        this.generator.addConstraint(constr, targetAttr.name)
    }

    // 添加计算清场时间的约束
    fun addCleanRuleConstraint(
        name: String,
        result: Attributes,
        lastTime: Attributes,
        startTime: Attributes,
        duration: Attributes
    ) {
        val constr = CleanRuleConstraint(name, result, lastTime, startTime, duration)
        this.generator.addConstraint(constr, result.name)
        this.generator.addCallbacks(constr)
    }

    fun collectInputs(): MutableMap<Int, List<AlgoObject>> {
        val res = mutableMapOf<Int, List<AlgoObject>>()
        res[1] = this.resources.getAll()
        res[2] = this.pieceSteps.getAll()
        res[3] = this.pieceStepOnResources.getAll()
        return res
    }

    fun collectConstraints(): List<Constraint> {
        return this.generator.getConstraints()
    }

    fun recover(model: FJSPSnapShot) {
        this.builder = model.objective
        for (constr in model.constraints) {
            this.generator.addConstraint(constr, constr.name)
        }
        for ((k, input) in model.inputs) {
            when (k) {
                1 -> this.addResources(input as List<PlanResource>)
                2 -> this.addEntities(input as List<PlanEntity>)
                3 -> this.addEntityOnResources(input as List<EntityOnResource>)
            }
        }
        for (variable in model.variables)
            this.generator.addVariable(variable)
    }

    fun findCriticalPath(solution: FJSPSolution): ArrayList<Pair<PlanEntity, PlanResource>> {
        val ans = ArrayList<Pair<PlanEntity, PlanResource>>()
//        val sequence = solution.result[solution.getLatestResource()]!!
//        val schedule = solution.assignment
//        val starEntity = sequence.findLastJob()//taskSequence.last()
//        addToCriticalPath(ans, starEntity, schedule, solution)
        return ans
    }

    fun getPreviousOperations(entity: PlanEntity): ArrayList<PlanEntity> {
        val operations = this.pieceStepNetwork!!.getPreviousOperations(entity)
        val ans = ArrayList<PlanEntity>()
        for (op in operations) {
            ans.add(this.getEntity(op))
        }
        return ans
    }

    private fun addToCriticalPath(
        ans: ArrayList<Pair<PlanEntity, PlanResource>>,
        block: Variable?,
        schedule: MutableMap<PlanEntity, Variable>,
        solution: FJSPSolution
    ) {
        if (block != null) {
            ans.add(Pair(block.block!!, block.locate!!))
            var jobSuccessor = false
            for (it in this.getPreviousOperations(block.block!!)) {
                if (schedule[block.block!!]!!.getStartTime() == schedule[it]!!.getEndTime()) {
                    jobSuccessor = true
                    addToCriticalPath(ans, schedule[it]!!, schedule, solution)
                }
            }
            if (!jobSuccessor) {
                val prev = block.previous
                if (prev != null) {
                    addToCriticalPath(ans, prev, schedule, solution)
                }
            }
        }
    }
}