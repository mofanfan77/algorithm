package core.entity

import utility.Util
import utility.annotation.Ignored


abstract class PlanEntity : AlgoObject {
    var topoIndex = 0
    var level1Id = ""
    var level2Id = 0.0
    var level3Id = 0.0
    var primaryId = ""
    var uniqueId = ""
    var productGroupId = ""
    var productId = ""
    var prevOpeartion = arrayListOf<Double>()
    var nextOpeartion = arrayListOf<Double>()
    var earlyTime = 0.0
    var dueTime = 0.0
    var lastStepFlag = false
    var quantity = 1.0

    /**
     * 多维资源约束下可用节点
     */
    private val availableGroupNodes = mutableMapOf<Int, ArrayList<String>>()
    @Deprecated("用 availableGroupNodes 代替")
    val availableNodes = ArrayList<String>()

    constructor()
    constructor(operationNr: Double, jobNr: String) {
        this.primaryId = jobNr
        this.level3Id = operationNr
        this.calcUniqueId()
    }

    override fun toString(): String {
        return uniqueId
    }

    fun calcUniqueId() {
        uniqueId = Util.getUniqueCodeOfEntity(this) //"$jobNr-$operationNr"
    }

    @Ignored
    fun isolated(): Boolean {
        return prevOpeartion.isEmpty() && nextOpeartion.isEmpty()
    }

    @Ignored
    fun getNodesByGroupIndex(groupIndex: Int): ArrayList<String> {
        val group0 = this.getGroups()[groupIndex]
        return this.availableGroupNodes[group0] ?: arrayListOf()
    }

    @Ignored
    fun getNodesByPrimaryGroup(): ArrayList<String> {
        val group0 = this.getGroups()[0]
        return this.availableGroupNodes[group0] ?: arrayListOf()
    }

    @Ignored
    fun getGroups(): List<Int>{
        return this.availableGroupNodes.keys.toList()
    }

    @Ignored
    fun addNodes(ent: PlanResource){
        this.availableGroupNodes.putIfAbsent(ent.groupId, arrayListOf())
        this.availableGroupNodes[ent.groupId]!!.add(ent.resourceId)
    }
}

class JobEntity : PlanEntity {
    var wipFlag = false
    var wipDuration = 0.0

    constructor() {}
    constructor(operationNr: Double, jobNr: String) : super(operationNr, jobNr) {}
}
