package config

import utility.enums.Option
import utility.enums.ParameterEnum
import utility.exception.ParameterTypeError

class JSPConfig : AlgorithmConfig() {
    // 策略模式
    private var flexiblePath = false
    // 评价器通用
    private var loopRetry = 5.0
    private var multiResource = false
    private var numberResource = 1

    override fun set(parameter: ParameterEnum, value: Any) {
        try {
            super.set(parameter, value)
            when (parameter.value) {
                Option.SolverTimeOption -> solveLimit = (value as Double)
                Option.flexiblePathOption -> flexiblePath = value as Boolean
                Option.iterationOption -> maxIteration = (value as Double)
                Option.loopRetry -> loopRetry = (value as Double)
                Option.multiResources -> multiResource = (value as Boolean)
                Option.numberResource -> numberResource = (value as Int)
                Option.popSize -> popSize = (value as Double)
                Option.randomMode -> deterministicFlag = (value as Boolean)
                Option.randomSeed -> randomSeed = (value as Int)
            }
        }catch (e: Exception){
            throw ParameterTypeError("${parameter.value}参数设置错误, 无法设定成${value}的类型, \n ${e.message}")
        }
    }

    override fun getAsDouble(key: ParameterEnum): Double {
        var ans : Double = 0.0
        when (key.value) {
            Option.loopRetry -> ans = loopRetry
        }
        return ans
    }

    override fun getAsBoolean(key: ParameterEnum): Boolean {
        var ans : Boolean = false
        when (key.value) {
            Option.multiResources -> ans = multiResource
            Option.randomMode -> ans = deterministicFlag
        }
        return ans
    }

    override fun getAsInt(key: ParameterEnum): Int {
        var ans : Int = 0
        when (key.value) {
            Option.numberResource -> ans = numberResource
            Option.randomSeed -> ans = randomSeed
        }
        return ans
    }
}