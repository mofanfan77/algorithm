package config

abstract class AlgorithmConfig : BaseConfig() {

    // 求解器通用
    var logLevel = -1
    var solveLimit = 10.0
    var deterministicFlag = true
    var randomSeed = 100

    // GA
    var maxIteration = 100.0
    var maxStagIteration = 25.0
    var mutationRatio = .1
    var crossoverRatio = .8
    var reproductionRatio = 0.05
    var popSize = 30.0


    // 自适应算子分数 和 衰减系数
    var improveScore = 2.5
    var bonusScore = 1.5
    var penaltyScore = 0.8
    var weightAlpha = 0.9

    // 退火
    var initialTemperature = 1000.0
    var temperatureUpdateRatio = 0.99

    // TS
    var maxTabuTenure = 15.0
    var maxTSIteration = 10.0
}