package analysis

import core.constraint.ObjectiveExecute
import core.entity.Variable
import utility.enums.ObjectiveEnum
import java.io.Serializable

class Metric : Serializable {
    var name: String = ""
    var weight: Double = 1.0
    var type: ObjectiveEnum = ObjectiveEnum.MINIMIZE
    var value = 0.0 // 修正后的值
    private var functionBody: ObjectiveExecute

    constructor(
        name: String,
        formula: (List<Variable>) -> Double,
        weight: Double = 1.0,
        type: ObjectiveEnum
    ) {
        this.name = name
        this.functionBody = ObjectiveExecute(formula)
        this.weight = weight
        this.type = type
    }

    constructor(name: String, exec: ObjectiveExecute,
                weight: Double = 1.0, type: ObjectiveEnum) {
        this.name = name
        this.functionBody = exec
        this.weight = weight
        this.type = type
    }

    override fun toString(): String {
        return this.name + "####" + this.value
    }

    fun getFitness(): Double {
        return when (type) {
            ObjectiveEnum.MAXIMIZE -> value * weight
            ObjectiveEnum.MINIMIZE -> -value * weight
        }
    }

    fun run(result: List<Variable>){
        this.value = this.functionBody.computeReturn(result)
    }

    fun create(): Metric {
        return Metric(name, functionBody, weight, type)
    }
}