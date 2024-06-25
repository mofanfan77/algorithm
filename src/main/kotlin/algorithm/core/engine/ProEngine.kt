package core.engine

import catalog.extensions.ConstraintType
import catalog.extensions.ObjectiveType
import catalog.extensions.VariableType
import catalog.extensions.callback.BCallBackHandler
import catalog.extensions.callback.CallBackHandler
import catalog.extensions.callback.ICallBackConstr
import catalog.extensions.range.*
import catalog.extensions.variable.BVariable
import catalog.extensions.variable.IVariable
import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBEnv
import com.gurobi.gurobi.GRBLinExpr
import com.gurobi.gurobi.GRBModel
import solver.solverLog
import utility.exception.InputDataValidationException

class ProEngine(name: String) : BaseEngine(name) {

    private var env: GRBEnv? = null
    private var model: GRBModel? = null
    private var sense: ObjectiveType = ObjectiveType.Minimize
    private var cbHandler: CallBackHandler = BCallBackHandler()

    init {
        // 链接
        env = GRBEnv(true)
    }

    override fun setEnv(configName: String, configValue: String) {
        env!!.set(configName, configValue)
    }

    override fun setEnv(configs: Map<String, String>) {
        for ((cn, cv) in configs) {
            env!!.set(cn, cv)
        }
    }

    override fun activateCallback(name: String): Boolean {
        val op = this.cbHandler.get(name)!!
        return op.checkStatus()
    }

    override fun start() {
        env!!.start()
        model = GRBModel(env)
    }

    override fun addLazyConstraint(name: String, lazyConstr: ConstraintEq, toDouble: Double) {
        val op = this.cbHandler.get(name)!!
        val s = this.createLeRange(lazyConstr, toDouble)
        op.addLazyConstraint(s)
    }

    override fun getCallBackValue(name: String, v: IVariable): Double {
        val op = this.cbHandler.get(name)!!
        return op.getCallBackValue(v)
    }

    override fun addCallback(func: (String) -> Unit, name: String) {
        this.cbHandler.create(name, func)
        val op = this.cbHandler.get(name)!!
        model!!.setCallback(op.getBCore())
    }

    override fun addNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, vararg name: String) {
        val keyName = name.joinToString("_")
        val newCache = (cache["default"] ?: hashMapOf()).toMutableMap()
        var variable = when (type) {
            VariableType.Float -> BVariable(model!!.addVar(lb, ub, obj, GRB.CONTINUOUS, keyName))
            VariableType.Integer -> BVariable(model!!.addVar(lb, ub, obj, GRB.INTEGER, keyName))
            VariableType.Binary -> BVariable(model!!.addVar(lb, ub, obj, GRB.BINARY, keyName))
        }
        newCache[keyName] = variable
        cache["default"] = newCache
    }

    override fun addGroupNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, groupName: String, vararg name: String) {
        val keyName = name.joinToString("_")
        val newCache = (cache[groupName] ?: hashMapOf()).toMutableMap()
        val variable = when (type) {
            VariableType.Float -> BVariable(model!!.addVar(lb, ub, obj, GRB.CONTINUOUS, keyName))
            VariableType.Integer -> BVariable(model!!.addVar(lb, ub, obj, GRB.INTEGER, keyName))
            VariableType.Binary -> BVariable(model!!.addVar(lb, ub, obj, GRB.BINARY, keyName))
        }
        newCache[keyName] = variable
        cache[groupName] = newCache
    }

    override fun setObjective(constraintEq: ConstraintEq, type: ObjectiveType) {
        sense = type
        val newExpr = getConstrFromExpr(constraintEq)
        when (type) {
            ObjectiveType.Minimize -> model!!.setObjective(newExpr, GRB.MINIMIZE)
            ObjectiveType.Maximize -> model!!.setObjective(newExpr, GRB.MAXIMIZE)
        }
    }

    override fun getVarByName(vararg name: String): IVariable {
        val defaultGroup = cache["default"] ?: hashMapOf()
        val keyName = name.joinToString("_")
        return defaultGroup[keyName] ?: throw InputDataValidationException("出现不存在的变量:$keyName")
    }

    override fun getVarByGroup(group: String, vararg name: String): IVariable? {
        val defaultGroup = cache[group] ?: hashMapOf()
        val keyName = name.joinToString("_")
        return defaultGroup[keyName]
    }

    override fun getObjectiveSense(): String {
        return if (sense == ObjectiveType.Maximize) {
            "Maximize"
        } else {
            "Minimize"
        }
    }

    override fun addConstr(expr: ConstraintExpr) {
        val newExpr = getConstrFromExpr(expr)
        model!!.addConstr(newExpr, GRB.LESS_EQUAL, expr.rhs, expr.name + "_LEFT")
        model!!.addConstr(newExpr, GRB.GREATER_EQUAL, expr.lhs, expr.name + "_RIGHT")
    }

    override fun addConstr(expr: ConstraintEq, type: ConstraintType, side: Double, name: String) {
        val newExpr = getConstrFromExpr(expr)
        when (type) {
            ConstraintType.eq -> model!!.addConstr(newExpr, GRB.EQUAL, side, name)
            ConstraintType.le -> model!!.addConstr(newExpr, GRB.LESS_EQUAL, side, name)
            ConstraintType.ge -> model!!.addConstr(newExpr, GRB.GREATER_EQUAL, side, name)
        }
        constrNum++
    }

    override fun solve(): Boolean {
        model!!.optimize()
        var flag = false
        val status = model!!.get(GRB.IntAttr.Status)
        solverLog.info("model solved at status: $status")
        if (status == GRB.Status.OPTIMAL) {
            flag = true
        }
        return flag
    }

    override fun export(path: String) {
        model!!.write(path)
    }

    override fun getStatus(): String {
        val status = model!!.get(GRB.IntAttr.Status)
        var finalStatus = when (status) {
            GRB.Status.OPTIMAL -> "OPTIMIAL"
            GRB.Status.INFEASIBLE -> "INFEASIBLE"
            GRB.Status.INF_OR_UNBD -> "INF_OR_UNBD"
            else -> "OTHER"
        }
        return finalStatus
    }

    override fun getNVars(): Int {
        return model!!.vars.size
    }

    override fun dispose() {
        cache.clear()
        model!!.dispose()
        env!!.dispose()
        model = null
        env = null
    }

    override fun getBestSolObj(): Double {
        return model!!.get(GRB.DoubleAttr.ObjVal)
    }

    override fun getBestSolVal(variable: IVariable?): Double {
        if (variable == null) {
            throw Exception("获取变量最优解失败，传入变量为空")
        }
        return variable.getBCore()!!.get(GRB.DoubleAttr.X)
    }

    override fun getBestSolValFromName(name: String): Double {
        var variable = this.getVarByName(name)
        return this.getBestSolVal(variable)
    }

    override fun readLP(path: String) {
        model!!.read(path)
    }

    override fun setTimeLimit(time: Double) {
        model!!.set("TimeLimit", time.toString())
    }

    fun setModel(configName: String, configValue: String) {
        model!!.set(configName, configValue)
    }

    override fun tuneParameter() {
        return
    }

    override fun addCallback(cb: ICallBackConstr) {
        return
    }

    override fun printStatistics() {
        return
    }

    override fun createLeRange(s: ConstraintEq, value: Double): IRange {
        val f1 = getConstrFromExpr(s)
        val obj = BRange(f1, leSense(), value)
        return obj
    }

    private fun getConstrFromExpr(expr: ConstraintEq): GRBLinExpr {
        val newExpr = GRBLinExpr()
        for (i in 0 until expr.coeffs.size) {
            newExpr.addTerm(expr.coeffs[i], expr.variables[i].getBCore())
        }
        return newExpr
    }
}