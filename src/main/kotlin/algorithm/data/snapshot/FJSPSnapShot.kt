package data.snapshot

import analysis.ObjectiveBuilder
import config.AlgorithmConfig
import core.constraint.Constraint
import core.entity.AlgoObject
import core.entity.Attributes

class FJSPSnapShot: ModelSnapshot() {
    // 目标
    lateinit var objective: ObjectiveBuilder
    // 约束
    lateinit var constraints:List<Constraint>
    // 变量
    lateinit var inputs : MutableMap<Int, List<AlgoObject>>
    //
    lateinit var variables : List<Attributes>
    // 配置
    lateinit var config: AlgorithmConfig
}