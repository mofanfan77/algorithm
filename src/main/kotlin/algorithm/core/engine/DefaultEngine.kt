package core.engine

import catalog.extensions.*
import catalog.extensions.callback.CallBackHandler
import catalog.extensions.callback.CallBackOperator
import catalog.extensions.callback.ICallBackConstr
import catalog.extensions.callback.XCallBackHandler
import catalog.extensions.range.ConstraintEq
import catalog.extensions.range.ConstraintExpr
import catalog.extensions.range.IRange
import catalog.extensions.range.XRange
import catalog.extensions.variable.IVariable
import catalog.extensions.variable.XVariable
import ilog.concert.IloNumExpr
import ilog.concert.IloNumVarType
import ilog.cplex.IloCplex
import solver.solverLog
import utility.exception.CallBackNotFoundException
import utility.exception.InputDataValidationException


class DefaultEngine(name: String) : BaseEngine(name) {

    private var model: IloCplex? = null
    private var sense: ObjectiveType = ObjectiveType.Minimize
    private var cbHandler : CallBackHandler = XCallBackHandler()

    init {
        model = IloCplex()
    }

    override fun addNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, vararg name: String) {
        val keyName = name.joinToString("_")
        val newCache = (cache["default"] ?: hashMapOf()).toMutableMap()
        val newVar = when (type) {
            VariableType.Float -> XVariable(model!!.numVar(lb, ub, IloNumVarType.Float, keyName).also { model!!.add(it) })
            VariableType.Integer -> XVariable(model!!.numVar(lb, ub, IloNumVarType.Int, keyName).also { model!!.add(it) })
            VariableType.Binary -> XVariable(model!!.numVar(lb, ub, IloNumVarType.Bool, keyName).also { model!!.add(it) })
        }
        newCache[keyName] = newVar
        cache["default"] = newCache
    }

    override fun addGroupNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, groupName: String, vararg name: String) {
        val keyName = name.joinToString("_")
        val newCache = (cache[groupName] ?: hashMapOf()).toMutableMap()
        newCache[keyName] = when (type) {
            VariableType.Float -> XVariable(model!!.numVar(lb, ub, IloNumVarType.Float, keyName).also { model!!.add(it) })
            VariableType.Integer -> XVariable(model!!.numVar(lb, ub, IloNumVarType.Float, keyName).also { model!!.add(it) })
            VariableType.Binary -> XVariable(model!!.numVar(lb, ub, IloNumVarType.Bool, keyName).also { model!!.add(it) })
        }
        cache[groupName] = newCache
    }

    override fun setObjective(constraintEq: ConstraintEq, type: ObjectiveType) {
        sense = type
        val newExpr = getConstrFromExpr(constraintEq)
        when (type) {
            ObjectiveType.Minimize -> model!!.addMinimize(newExpr)
            ObjectiveType.Maximize -> model!!.addMaximize(newExpr)
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

    private fun getConstrFromExpr(expr: ConstraintEq): IloNumExpr {
        val newExpr = model!!.linearNumExpr()
        for (i in 0 until expr.coeffs.size) {
            newExpr.addTerm(expr.coeffs[i], expr.variables[i].getXCore())
        }
        return newExpr
    }

    override fun addConstr(expr: ConstraintEq, type: ConstraintType, side: Double, name: String) {
        val newExpr = getConstrFromExpr(expr)
        when (type) {
            ConstraintType.eq -> model!!.addEq(newExpr, side, name)
            ConstraintType.le -> model!!.addLe(newExpr, side, name)
            ConstraintType.ge -> model!!.addGe(newExpr, side, name)
        }
        constrNum++
    }

    override fun addConstr(expr: ConstraintExpr) {
        val newExpr = getConstrFromExpr(expr)
        model!!.addLe(newExpr, expr.rhs, expr.name + "_LEFT")
        model!!.addGe(newExpr, expr.lhs, expr.name + "_RIGHT")
    }

    override fun solve(): Boolean {
        val flag = model!!.solve()
        solverLog.info("model solved at status: ${model!!.status}")
        return flag
    }

    override fun getStatus(): String {
        return model!!.status.toString()
    }

    override fun getNVars(): Int {
        return model!!.ncols
    }

    override fun dispose() {
        cache.clear()
        model!!.clearModel()
        model!!.endModel()
        model!!.end()
    }

    override fun getBestSolObj(): Double {
        return if (model!!.isMIP) {
            model!!.bestObjValue
        } else {
            model!!.objValue
        }
    }

    override fun getBestSolVal(variable: IVariable?): Double {
        if (variable == null) {
            throw Exception("获取变量最优解失败，传入变量为空")
        }
        return model!!.getValue(variable.getXCore())
    }

    override fun getBestSolValFromName(name: String): Double {
        val variable = this.getVarByName(name)
        return this.getBestSolVal(variable)
    }

    override fun readLP(path: String) {
        model!!.importModel(path)
    }

    override fun tuneParameter() {
        model!!.setParam(IloCplex.Param.RootAlgorithm, IloCplex.Algorithm.Auto)
        model!!.setParam(IloCplex.Param.Parallel, IloCplex.ParallelMode.Deterministic)
        model!!.setParam(IloCplex.Param.TimeLimit, 30.0)
        model!!.setParam(IloCplex.Param.Tune.Display, 1)
        model!!.setParam(IloCplex.Param.WorkMem, 9216.0)
//        model!!.setOut(null)
    }

    override fun setTimeLimit(time: Double) {
        model!!.setParam(IloCplex.Param.TimeLimit, time)
    }

    fun setWorkMemory(size: Double) {
        model!!.setParam(IloCplex.Param.WorkMem, size)
    }

    override fun addCallback(func: (String) -> Unit, name: String) {
        this.cbHandler.create(name, func)
        val op = this.cbHandler.get(name)!!
        model!!.use(op.getXCore())
    }

    override fun getCallBack(name:String): CallBackOperator {
        return this.cbHandler.get(name) ?: throw CallBackNotFoundException("callback $name not exist")
    }

    override fun addLazyConstraint(name: String, lazyConstr: ConstraintEq, toDouble: Double){
        val op = this.cbHandler.get(name)!!
        val s = this.createLeRange(lazyConstr, toDouble)
        op.addLazyConstraint(s)
    }

    override fun getCallBackValue(name:String, v: IVariable): Double{
        val op = this.cbHandler.get(name)!!
        return op.getCallBackValue(v)
    }

    override fun printStatistics() {
    }

    override fun export(path: String) {
        model!!.exportModel(path)
    }

    override fun addCallback(cb: ICallBackConstr) {
        model!!.use(cb.getCoreC())
    }

    override fun createLeRange(s: ConstraintEq, value: Double): IRange {
        val f1 = getConstrFromExpr(s)
        val newRange = model!!.range(-Double.MAX_VALUE, f1, value)
        val obj = XRange(newRange, leSense(), value)
        return obj
    }
}