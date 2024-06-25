package catalog.extensions.range

import catalog.extensions.ConstraintType

abstract class RangeClass : IRange {
    var rightHandValue = 0.0
    var rangeSense = ConstraintType.le

    override fun getSense(): ConstraintType {
        return this.rangeSense
    }

    override fun getValue(): Double {
        return this.rightHandValue
    }
}