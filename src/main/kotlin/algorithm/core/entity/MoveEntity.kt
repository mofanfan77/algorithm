package core.entity

/**
 * 带坐标的任务
 */
class MoveEntity: PlanEntity() {
    var taskId = ""
    var algoNr = 0
    var origin: NodeEntity = NodeEntity()
    var dest: NodeEntity = NodeEntity()
    // 起点停留时长
    var originDelay : Int = 0
    // 终点停留时长
    var destDelay : Int = 0
    var status = true
    // 依赖前序任务
    var dependentTask: MoveEntity? = null
    // 是否可以更换行车(和前序任务相比）
    var canChangeCraneFlag = false
    // 终点候选点
    var destCandidates = arrayListOf<NodeEntity>()
    var destSelectionMode = false
    private var processed = false

    fun getDistance():Double{
        return Coordinate.getOneDimDistance(origin, dest)
    }
}