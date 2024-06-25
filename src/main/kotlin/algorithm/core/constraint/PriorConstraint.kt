package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class PriorConstraint(name: String) : Constraint(name) {

    constructor(name: String, resultAttr: Attributes, sourceAttr: Attributes) : this(name) {
        this.sourceAttr = resultAttr
        this.targetAttrs = arrayListOf(sourceAttr)
    }

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        val related = vars.priorVariables
        // from是因变，to是自变量
        var newVal = vars[source].value as Double
        for (pr in related) {
            val prevVal = pr[targetAttrs[0]].value as Double
            newVal = maxOf(prevVal, newVal)
        }
        val newWalConsiderCalendar = vars.owner!!.findNextAvailableTime(newVal, 0.0, vars)
        vars[source].value = newWalConsiderCalendar
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            atn.connectWithPrior(ele, sourceAttr, targetAttrs[0])
        }
    }

//    private fun calcDependency(value: Double, curr: Variable, prev: Variable) : Double{
//        // 根据前置变量计算依赖时间
//        var result = value
//        if (curr.relation!!.startTogether) {
//            val post = if (!prev.relation!!.postDurationFlag) 0.0 else prev.relation!!.postDuration
//            result -= (prev.relation!!.duration + post)
//        } else if (curr.relation!!.endTogether) {
//            val post = if (!curr.relation!!.postDurationFlag) 0.0 else curr.relation!!.postDuration
//            result -= (curr.relation!!.duration + post)
//        } else {
//            result -= curr.relation!!.moveEarlier
//        }
//        return result
//    }
}