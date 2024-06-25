package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable
import utility.enums.RuleEnum

class CleanRuleConstraint(name: String) : Constraint(name) {
    constructor(name: String, targetAttr: Attributes, lastTimeAttr: Attributes,
                startAttr: Attributes, durationAttr: Attributes) : this(name) {
        this.targetAttrs = arrayListOf(lastTimeAttr, startAttr, durationAttr)
        this.sourceAttr = targetAttr
    }

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        var value = 0.0
        // true 为作业优先
        val jobFirstFlag = vars.relation!!.jobFirstFlag
        // 上一次清场时间
        var threshValue = 0.0
        var durationValue = 0.0
        when (name) {
            RuleEnum.MaxClean.desc -> {
                threshValue = vars.relation!!.maxCleanThreshValue
                durationValue = vars.relation!!.maxCleanDuration
            }

            RuleEnum.MinClean.desc -> {
                threshValue = vars.relation!!.minCleanThreshValue
                durationValue = vars.relation!!.minCleanDuration
            }
        }
        val lastValue = vars[targetAttrs[0]].value as Double
        val startTime = vars[targetAttrs[1]].value as Double
        val endTime = Attributes.addValue(vars[targetAttrs[1]].value, vars[targetAttrs[2]].value) as Double
        if (startTime - lastValue >= threshValue) { // 肯定要清场
            value = durationValue
        } else if (endTime - lastValue <= threshValue) { // 肯定不用清场
        } else {
            if (!jobFirstFlag) {
                value = durationValue
            }
        }
        vars[source].value = value
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            for (target in targetAttrs) {
                atn.connect(ele[sourceAttr], ele[target])
            }
        }
    }
}