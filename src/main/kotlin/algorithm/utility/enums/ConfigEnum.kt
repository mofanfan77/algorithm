package utility.enums

import utility.enums.Option.SolverTimeOption
import utility.enums.Option.flexiblePathOption
import utility.enums.Option.iterationOption
import utility.enums.Option.loopRetry
import utility.enums.Option.multiResources
import utility.enums.Option.numberResource
import utility.enums.Option.popSize
import utility.enums.Option.randomMode
import utility.enums.Option.randomSeed

enum class ConfigEnum(val value: Int, val desc: String) {
    NoLogConfig(0, "无日志模式"),
    FullLogConfig(1, "全日志模式"),
    SemiLogConfig(2, "半日志模式")
}

enum class TimeUnit(val value: Int, val desc: String){
    seconds(0, "秒"),
    minutes(1, "分钟"),
    hours(2, "小时"),
    days(3, "天"),
}

enum class ParameterEnum(val value: String, val desc: String) {
    FlexiblePath(flexiblePathOption, "多工艺路径模式"),
    SolverTime(SolverTimeOption, "求解时间"),
    IterationLimit(iterationOption, "迭代次数"),
    LoopRetry(loopRetry, "规则引擎计算循环最大次数"),
    MultiResource(multiResources, "多资源模式"),
    NumberResource(numberResource, "多资源数量"),
    GenerationPoolSize(popSize, "种群大小"),
    RandomSeed(randomSeed, "随机种子"),
    Mode(randomMode, "求解模式"),
}

object Option {
    const val flexiblePathOption = "flexiblePath"
    const val SolverTimeOption = "solverTime"
    const val iterationOption = "iterationLimit"
    const val loopRetry = "loopRetry"
    const val multiResources = "multiResource"
    const val numberResource = "numberResource"
    const val popSize = "populationSize"
    const val randomMode = "randomnessMode"
    const val randomSeed = "randomNumberSeed"
}