package catalog.extensions.variable

import ilog.concert.IloNumVar

class XVariable: IVariable {
    lateinit var core: IloNumVar

    constructor()
    constructor(core: IloNumVar){
        this.core = core
    }
    override fun getXCore(): IloNumVar {
        return core
    }
}