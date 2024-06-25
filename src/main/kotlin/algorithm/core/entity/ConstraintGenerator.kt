package core.entity

import core.constraint.CleanRuleConstraint
import core.constraint.Constraint
import solver.solverLog
import utility.exception.ConstrDuplicateException

class ConstraintGenerator : AttributeManager() {
    private val constraints = mutableMapOf<String, Constraint>()
    private val reoptConstrs = mutableMapOf<String, Constraint>()
    private var callbacks = mutableListOf<Constraint>()

    fun addConstraint(constr: Constraint, name: String, reoptFlag: Boolean = false) {
        if (name in this.constraints) {
            throw ConstrDuplicateException("存在重复约束名称, $name")
        }
        if (reoptFlag) {
            this.reoptConstrs[name] = constr
            constr.sourceAttr.reopt = true
        } else {
            this.constraints[name] = constr
        }
    }

    fun addCallbacks(constr: CleanRuleConstraint) {
        this.callbacks.add(constr)
    }

    fun getConstraints(): List<Constraint> {
        return this.constraints.values.toList()
    }

    fun getConstr(key: Attributes): Constraint? {
        return if (key.reopt){
            this.reoptConstrs[key.name]
        }else{
            this.constraints[key.name]
        }
    }

    fun InitialIterator(): Iterator<Constraint> {
        return constraints.values.iterator()
    }

    fun SecondaryIterator(): Iterator<Constraint> {
        return reoptConstrs.values.iterator()
    }

    fun updateStatistics(){
        for (con in constraints.values){
            con.count()
        }
        for (con in reoptConstrs.values){
            con.count()
        }
        for (con in callbacks){
            con.count()
        }
    }

    fun printStaistics(){
        val totalLength = 25
        var statLogs = "|| -- constraint names -- || calc. cnt || avg calc. time"
        val newConstrs = ArrayList<Constraint>()
        newConstrs.addAll(constraints.values)
        newConstrs.addAll(reoptConstrs.values)
        newConstrs.addAll(callbacks)
        for (con in newConstrs.sortedByDescending { it.totalTime() }){
            statLogs += "\n|| ${con.name.padEnd(totalLength, ' ') } || ${con.getCnt()} || ${con.getAvg()} || ${con.getTime()}"
        }
        solverLog.info(statLogs)
    }
}