package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable

class CustomDepencyConstraint(name: String) : Constraint(name) {

    lateinit var dependency: DependencyExecuteByVariable

    constructor(name: String, func: (Variable, List<Variable>, Attributes, List<Attributes>) -> Unit,
                dependency: (Variable) -> List<Variable>, target: Attributes, param: List<Attributes>
    ) : this(name) {
        this.sourceAttr = target
        this.targetAttrs = param
        this.executable = ConstraintExecute { a, b, c ->
            val deps = dependency(a)
            func(a, deps, b, c)
        }
    }

    override fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>) {
        for (entry in variableList) {
            val ele = entry.value
            val dep = this.dependency.compute(ele)
            for (prev in targetAttrs) {
                for (tg in dep) {
                    atn.connect(ele[sourceAttr], tg[prev])
                }
            }
        }
    }
}