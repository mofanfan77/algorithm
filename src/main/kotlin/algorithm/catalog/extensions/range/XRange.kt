package catalog.extensions.range

import catalog.extensions.ConstraintType
import ilog.concert.IloRange

class XRange : IRange, RangeClass {
    lateinit var core: IloRange


    constructor()
    constructor(core: IloRange) {
        this.core = core
    }

    constructor(core: IloRange, sense: ConstraintType, value: Double) {
        this.core = core
        this.rightHandValue = value
        this.rangeSense = sense
    }

    override fun getXCore(): IloRange {
        return core
    }
}
