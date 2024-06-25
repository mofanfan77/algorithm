package catalog.component

import core.entity.PlanEntity
import utility.Util
import java.lang.Exception
import kotlin.collections.ArrayList

class EntityNetwork {
    /**
     * 工序的入度
     * key = uniqueId
     */
    private var inbound = mutableMapOf<PlanEntity, ArrayList<PlanEntity>>()

    /**
     * 工序的出度
     * key = uniqueId
     */
    private var outbound = mutableMapOf<PlanEntity, ArrayList<PlanEntity>>()

    /**
     * 每个工件job上的入度出度
     * key = pieceNr
     */
    var sameJobInbound = mutableMapOf<String, MutableMap<PlanEntity, ArrayList<PlanEntity>>>()

    /**
     * 每个工件job的拓扑排序
     * key = uniqueId
     */
    var topoOrderByJob = mutableMapOf<String, ArrayList<PlanEntity>>()

    /**
     * 每个job下无依赖的工序
     */
    var singleJobs = mutableMapOf<String, ArrayList<PlanEntity>>()

    private fun updateSameJobMap(prevCode: PlanEntity, afterCode: PlanEntity) {
        val piece = Util.parsePieceNrFromRealCode(prevCode.uniqueId)
        // 出度表
        try {
            val jobOutbound = sameJobInbound[piece]!!
            val sameOutbound = jobOutbound.getOrDefault(prevCode, ArrayList())
            sameOutbound.add(afterCode)
            jobOutbound[prevCode] = sameOutbound
            sameJobInbound[piece] = jobOutbound
        }catch (e: NullPointerException){
            throw Exception("规划资源列表中, 无法找到 $piece")
        }
    }

    /**
     * 根据 pieceStep 添加
     */
    fun add(entity: PlanEntity) {
        inbound.putIfAbsent(entity, arrayListOf())
        outbound.putIfAbsent(entity, arrayListOf())
        sameJobInbound.putIfAbsent(entity.primaryId, mutableMapOf())
        if (entity.isolated()) {
            singleJobs.putIfAbsent(entity.primaryId, arrayListOf())
            singleJobs[entity.primaryId]!!.add(entity)
        }
    }

    fun getInDegreeMap(): MutableMap<PlanEntity, ArrayList<PlanEntity>> {
        return this.inbound
    }

    fun connect(prevCode: PlanEntity, afterCode: PlanEntity) {
        val isSameJob = Util.parsePieceNrFromRealCode(prevCode.uniqueId) == Util.parsePieceNrFromRealCode(afterCode.uniqueId)
        // 出度表
        inbound[afterCode]?.add(prevCode)
        //入度表
        outbound[prevCode]?.add(afterCode)
        // 同工件的顺序记录
        if (isSameJob) {
            updateSameJobMap(prevCode, afterCode)
        }
    }

    fun topoLogicalOrder() {
        for ((k, u) in singleJobs){
            val tempJobs = arrayListOf<PlanEntity>()
            tempJobs.addAll(u)
            topoOrderByJob[k] = tempJobs
        }
        for ((k, u) in sameJobInbound) {
            val tempJobs = arrayListOf<PlanEntity>()
            val sortedOrder = Util.topoSort(u)
            tempJobs.addAll(topoOrderByJob[k]?.toMutableList() ?: arrayListOf())
            tempJobs.addAll(sortedOrder)
            topoOrderByJob[k] = tempJobs
        }
    }

    fun getPrevEntity(afterCode: PlanEntity): ArrayList<PlanEntity>{
        return this.inbound[afterCode] ?: arrayListOf()
    }
    fun getAfterEntity(prev: PlanEntity): ArrayList<PlanEntity>{
        return this.outbound[prev] ?: arrayListOf()
    }

    fun getPreviousOperations(afterCode: PlanEntity): ArrayList<String> {
        val ans = ArrayList<String>()
        val afterSteps = this.inbound[afterCode]
        if (afterSteps != null) {
            ans.addAll(afterSteps.map { it.uniqueId })
        }
        return ans
    }
}