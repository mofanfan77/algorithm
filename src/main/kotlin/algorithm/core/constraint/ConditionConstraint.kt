package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class ConditionConstraint(name: String) : Constraint(name) {
    constructor(name: String, resultAttr: Attributes, startAttr: Attributes, targetValue: Attributes,
                trueVal: Attributes, falseVal: Attributes
    ) : this(name) {
        this.sourceAttr = resultAttr
        this.targetAttrs = arrayListOf(startAttr, targetValue, trueVal, falseVal)
    }

    override var executable = ConstraintExecute { vars, source, targetAttrs ->
        var flag = false
        val startAttr: Attributes = targetAttrs[0]
        val targetValue: Attributes = targetAttrs[1]
        val trueVal: Attributes = targetAttrs[2]
        val falseVal: Attributes = targetAttrs[3]
        if (targetValue.name == "") { // 标量
            if (Attributes.equalsTo(targetValue.value, vars[startAttr].value)) {
                flag = true
            }
        } else {
            if (Attributes.equalsTo(vars[targetValue].value, vars[startAttr].value)) {
                flag = true
            } else if (vars[targetValue].isEmpty()) {
                flag = true
            }
        }
        var ans: Any?
        if (flag) {
            if (trueVal.name != "") {
                ans = vars[trueVal].value
            } else {
                ans = trueVal.value
            }
        } else {
            if (falseVal.name != "") {
                ans = vars[falseVal].value
            } else {
                ans = falseVal.value
            }
        }
        vars[source].value = ans
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            val startAttr: Attributes = targetAttrs[0]
            val targetValue: Attributes = targetAttrs[1]
            val trueVal: Attributes = targetAttrs[2]
            val falseVal: Attributes = targetAttrs[3]
            if (startAttr.name != "") {
                atn.connect(ele[sourceAttr], ele[startAttr])
            }
            if (targetValue.name != "") {
                atn.connect(ele[sourceAttr], ele[targetValue])
            }
            if (trueVal.name != "") {
                atn.connect(ele[sourceAttr], ele[trueVal])
            }
            if (falseVal.name != "") {
                atn.connect(ele[sourceAttr], ele[falseVal])
            }
        }
    }
}