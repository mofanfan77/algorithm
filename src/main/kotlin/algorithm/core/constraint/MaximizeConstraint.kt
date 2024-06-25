package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class MaximizeConstraint(name: String) : Constraint(name) {

    constructor(name: String, result: Attributes, attrList: Array<out Attributes>) : this(name) {
        this.sourceAttr = result
        this.targetAttrs = arrayListOf(*attrList)
    }

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        var value: Any = vars[targetAttrs[0]].value
        for (it in targetAttrs) {
            value = Attributes.getMaxOf(value, vars[it].value)
        }
        vars[source].value = value
    }


    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            for (attr in targetAttrs) {
                atn.connect(ele[sourceAttr], ele[attr])
            }
        }
    }
}