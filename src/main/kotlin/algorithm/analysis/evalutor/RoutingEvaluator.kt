package analysis.evalutor

import catalog.ea.ChromoseBase
import config.BaseConfig
import utility.annotation.Ignored

class RoutingEvaluator : BaseEvaluator(){
    override fun decoder(chromosome: ChromoseBase) {
        return
    }

    @Ignored
    override fun debugRun(chromosome: ChromoseBase) {
        return
    }

    @Ignored
    override fun getParameter(config: BaseConfig) {
        return
    }
}