package core.constraint

import core.entity.*

class SubseqConstraint(name: String) : Constraint(name) {

    constructor(name: String, source: Attributes, targetAttr: Attributes) : this(name) {
        this.targetAttrs = arrayListOf(targetAttr)
        this.sourceAttr = source
    }

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        val related = vars.subsequenVariables
        var newVal = vars[targetAttrs[0]].value as Double
        for (pr in related) {
            val prevVal = pr[targetAttrs[0]].value as Double
            newVal = maxOf(prevVal, newVal)
        }
        vars[source].value = newVal
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            atn.connectWithSubseq(entry.value, sourceAttr, targetAttrs[0])
        }
    }
}