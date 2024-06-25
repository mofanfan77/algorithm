package catalog.extensions.range

import catalog.extensions.ConstraintType
import com.gurobi.gurobi.GRBLinExpr

class BRange : IRange, RangeClass {
    lateinit var core: GRBLinExpr

    constructor()
    constructor(core: GRBLinExpr){
        this.core = core
    }

    constructor(core: GRBLinExpr, sense: ConstraintType, value: Double){
        this.core = core
        this.rightHandValue = value
        this.rangeSense = sense
    }

    override fun getBCore(): GRBLinExpr {
        return core
    }

}