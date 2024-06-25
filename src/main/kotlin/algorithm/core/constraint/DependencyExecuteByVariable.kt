package core.constraint

import core.entity.Attributes
import core.entity.Variable
import java.io.Serializable

fun interface ObjectiveExecute : Serializable{
    fun computeReturn(a: List<Variable>): Double
}

fun interface ConstraintExecute: Serializable{
    fun compute(variable: Variable, source: Attributes, targets: List<Attributes>)
}

class ConstrainExecuteByDependency: ConstraintExecute{
    lateinit var dependecy : DependencyExecuteByVariable
    override fun compute(variable: Variable, source: Attributes, targets: List<Attributes>) {
        var deps = dependecy.compute(variable)

    }
}

fun interface DependencyExecuteByVariable: Serializable{
    fun compute(variable: Variable ): List<Variable>
}