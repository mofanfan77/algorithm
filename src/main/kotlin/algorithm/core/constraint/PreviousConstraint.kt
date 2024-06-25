package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class PreviousConstraint(name: String) : Constraint(name) {

    constructor(name: String, source: Attributes, targetAttr: Attributes) : this(name) {
        this.sourceAttr = source
        this.targetAttrs = arrayListOf(targetAttr)
    }

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        if (vars.previous != null) {
            vars[source].value = vars.previous!![targetAttrs[0]].value
        }
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            ele.previous?.let { prevNode ->
                atn.connect(ele[sourceAttr], prevNode[targetAttrs[0]])
            }
        }
    }
}