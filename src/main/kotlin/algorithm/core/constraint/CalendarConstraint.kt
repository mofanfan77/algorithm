package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class CalendarConstraint(name: String) : Constraint(name) {

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        val startTime = vars[targetAttrs[0]].value as Double
        val duration = vars[targetAttrs[1]].value as Double
        val origValue = vars[source].value as Double
        val newValue = vars.owner!!.findNextAvailableTime(startTime, duration, vars)
        vars[source].value = maxOf(origValue, newValue)
    }

    constructor(name: String,
                resultAttr: Attributes,
                startAttr: Attributes,
                durationAttr: Attributes) : this(name) {
        this.sourceAttr = resultAttr
        this.targetAttrs = arrayListOf(startAttr, durationAttr)
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            for (attr in this.targetAttrs) {
                atn.connect(ele[sourceAttr], ele[attr])
            }
        }
    }
}