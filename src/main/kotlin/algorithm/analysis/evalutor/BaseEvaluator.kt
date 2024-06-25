package analysis.evalutor

import catalog.ea.ChromoseBase
import config.BaseConfig

abstract class BaseEvaluator {
    lateinit var config: BaseConfig
    abstract fun decoder(chromosome: ChromoseBase)
    abstract fun debugRun(chromosome: ChromoseBase)
    abstract fun getParameter(config: BaseConfig)

//    abstract suspend fun decoderInParallel(chromosome: Chromosome): Int
}