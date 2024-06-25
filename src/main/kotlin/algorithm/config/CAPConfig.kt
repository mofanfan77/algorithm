package config

import utility.annotation.Ignored
import utility.enums.ParameterEnum

class CAPConfig : AlgorithmConfig(){
    // 安全距离
    var safetyInterval = 5.0
    // 时间长度
    var timeHorizon = 30
    // 跨的最大长度
    var maxLength = 1000.0

    //求解相关参数
    // 求解模式 （0 - 大规模场景, 1 - 小规模场景
    var mode = 0
    // lp文件输出
    var logFlag = true
    // 求解时间
    var timeLimit = 30.0
    // 文件路径
    var logPath = ""
    // 问题类型
    var nodeSelection = false

    @Ignored()
    override fun set(parameter: ParameterEnum, value: Any) {
        super.set(parameter, value)
        return
    }
}