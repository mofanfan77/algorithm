package solver.datainterface

import core.entity.MoveEntity
import core.entity.MoveResource
import core.entity.NodeEntity
import data.dataobject.CAPResultData
import data.dataobject.CAPTraceData

interface CAPInterface {

    /**
     * 增加任务
     */
    fun addTasks(tasks: MutableList<MoveEntity>)

    /**
     * 增加车辆
     */
    fun addCranes(resources: MutableList<MoveResource>)


    fun addRepairList(repairList: MutableList<NodeEntity>)
    /**
     * 增加工位
     */
    fun addNodes(nodes: List<NodeEntity>)

    // 结果处理
    fun getCraneTrace(): List<CAPTraceData>

    fun getAssignment(): List<CAPResultData>
}