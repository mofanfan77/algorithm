package core.entity

import utility.unicodeConnector
import java.time.Duration
import java.time.LocalDateTime

class EntityOnResource : AlgoObject() {
    var resourceId = ""
    var pieceNr = ""
    var sequenceNr = .0
    var jobFirstFlag = false
    var startFlag = false
    // 前处理时长类型
    var maxCleanDuration = 0.0
    var maxCleanThreshValue = 0.0
    var minCleanDuration = 0.0
    var minCleanThreshValue = 0.0

    // 加工时长
    var duration = 0.0

    // 切换时长
    var switchMinTag = ""
    var switchDurationMax = 0.0
    var switchDurationMin = 0.0

    // 后处理时长
    var postDuration = 0.0
    var affectNextStepFlag = false
    var postDurationFlag: Boolean = false // 后处理产能使用情况

    // vice
    var subResourceUtil = mutableMapOf<String, Double>()

    // 时间依赖相关参数
    var moveEarlier = 0.0
    var startTogether = false
    var endTogether = false
    var hardConstraintFlag = false

    var entityId = ""
    fun calcEntityId() {
        this.entityId = "${pieceNr}${unicodeConnector}${sequenceNr.toInt()}"
    }
}