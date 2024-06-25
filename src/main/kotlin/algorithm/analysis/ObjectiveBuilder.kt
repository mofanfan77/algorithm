package analysis

import core.entity.Variable
import utility.enums.ObjectiveEnum
import utility.exception.ObjectiveLevelNotExistError
import java.io.Serializable
import java.util.*

class ObjectiveBuilder : Serializable {

    var metrics = TreeMap<Int, ArrayList<Metric>>()
    var tolerance = TreeMap<Int, Double>()
    /**
     * 添加目标函数
     */
    fun addObjective(
        name: String,
        formula: (List<Variable>) -> Double,
        weight: Double,
        type: ObjectiveEnum,
        level:Int = 1
    ) {
        val metric = Metric(name, formula, weight, type)
        metrics.putIfAbsent(level, arrayListOf())
        metrics[level]!!.add(metric)
        tolerance.putIfAbsent(level, 0.0)
    }

    @Deprecated("目标函数名已废弃")
    fun addObjective(
        name: String,
        desc: String,
        formula: (List<Variable>) -> Double,
        weight: Double,
        type: ObjectiveEnum
    ) {
//        val metric = Metric(name, desc, formula, weight, type)
//        metrics.addLazyConstraint(metric)
    }

    fun build(): Objective {
        val newObj = Objective()
        newObj.initial(this)
        return newObj
    }

    fun setObjectiveTolerance(level: Int, relativeTolerance: Double) {
        if (level !in this.tolerance){
            throw ObjectiveLevelNotExistError("设置的层级未事先定义, 层级${level} - $relativeTolerance")
        }
        this.tolerance[level] = relativeTolerance
    }
}