package core.constraint

import core.constraint.Constraint
import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class CustomConstraint(name: String) : Constraint(name) {

    constructor(name: String, func: (Variable, Attributes, List<Attributes>) -> Unit, target: Attributes,
                param: List<Attributes>
    ) : this(name) {
        this.sourceAttr = target
        this.targetAttrs = param
        this.executable = ConstraintExecute(func)
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            for (prev in targetAttrs) {
                atn.connect(ele[sourceAttr], ele[prev])
            }
        }
    }
}