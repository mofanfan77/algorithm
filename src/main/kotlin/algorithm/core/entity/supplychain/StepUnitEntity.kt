package core.entity.supplychain

import core.entity.AlgoObject

class StepUnitEntity : AlgoObject() {
    /**
     * 唯一主键
     */
    var uniqueId = ""

    /**
     * 工艺路径
     */
    var routeId = ""

    /**
     * 产品id
     */
    var productId = ""

    /**
     * 工艺路径-步骤id
     */
    var stepId = ""

    /**
     * 序号
     */
    var seqNr = 0

    /**
     * 设备id
     */
    var unitId = ""

    /**
     * 批量大小
     */
    var batchSize = 1

    /**
     * 是否为输出
     */
    var outputFlag = false

    /**
     * 加工时间
     */
    var durationTime = 1.0

    /**
     * 转换系数
     */
    var ratio = 1.0
}