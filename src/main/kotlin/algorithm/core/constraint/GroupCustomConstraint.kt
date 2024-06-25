package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class GroupCustomConstraint(name: String): Constraint(name){

    constructor(groupName: String, name: String, func: (Variable, Attributes, List<Attributes>, String) -> Unit, target: Attributes,
                param: List<Attributes>
    ) : this(name) {
        this.sourceAttr = target
        this.targetAttrs = param
        this.groupName = groupName
        this.executable = ConstraintExecute{a,b,c ->
            func(a,b,c, this.groupName)
        }
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