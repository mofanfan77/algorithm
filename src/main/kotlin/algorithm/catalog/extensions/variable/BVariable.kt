package catalog.extensions.variable

import catalog.extensions.variable.IVariable
import com.gurobi.gurobi.GRBVar

class BVariable : IVariable {
    lateinit var core: GRBVar

    constructor()
    constructor(core: GRBVar) {
        this.core = core
    }

    override fun getBCore(): GRBVar {
        return core
    }
}