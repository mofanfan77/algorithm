package core.entity

open class MoveResource : PlanResource() {
    // 速度
    var speed = 50.0
    var x = 0.0
    var y = 0.0
    // 移动下限
    var lowBound = 0.0
    // 移动上限
    var upBound = Double.MAX_VALUE
    var algoNr = 0
    // TODO currentEntity added
    var currentEntity : String?= ""
    // 状态(0 = 正常， 1= 活车检修  2= 死车检修)
    var status = 0
}