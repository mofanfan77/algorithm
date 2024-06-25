package core.engine

import catalog.extensions.*
import catalog.extensions.callback.ICallBackConstr
import catalog.extensions.range.ConstraintEq
import catalog.extensions.range.ConstraintExpr
import catalog.extensions.range.IRange
import catalog.extensions.range.XRange
import catalog.extensions.variable.GVariable
import catalog.extensions.variable.IVariable
import jscip.SCIP_Vartype
import jscip.Scip
import jscip.Solution
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.exception.InputDataValidationException
import kotlin.Exception

val modelLog: Logger = LoggerFactory.getLogger(GDMIPEngine::class.java)
class GDMIPEngine(name: String) : BaseEngine(name) {

    private var model: Scip? = null
    var objective: Solution? = null

    init {
        try {
            System.loadLibrary("jscip")
            val scip = Scip()
            scip.create(name)
            model = scip
        }catch (e: Exception){
            modelLog.error("生成MIP模型失败, ${e.printStackTrace()}")
        }
    }

    /**
     * 新增变量
     */
    override fun addNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, vararg name: String){
        val keyName = name.joinToString("_")
        val newCache = (cache["default"] ?: hashMapOf()).toMutableMap()
        var variable = when (type) {
            VariableType.Float -> GVariable(model!!.createVar(keyName, lb, ub, obj, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS))
            VariableType.Integer -> GVariable(model!!.createVar(keyName, lb, ub, obj, SCIP_Vartype.SCIP_VARTYPE_INTEGER))
            VariableType.Binary -> GVariable(model!!.createVar(keyName, lb, ub, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY))
        }
        newCache[keyName] = variable
        cache["default"] = newCache
    }

    override fun addGroupNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, groupName: String, vararg name: String){
        val keyName = name.joinToString("_")
        val newCache = (cache[groupName] ?: hashMapOf()).toMutableMap()
        newCache[keyName] = when (type) {
            VariableType.Float -> GVariable(model!!.createVar(keyName, lb, ub, obj, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS))
            VariableType.Integer -> GVariable(model!!.createVar(keyName, lb, ub, obj, SCIP_Vartype.SCIP_VARTYPE_INTEGER))
            VariableType.Binary -> GVariable(model!!.createVar(keyName, lb, ub, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY))
        }
        cache[groupName] = newCache
    }

    fun addNumVars(lb: Double, ub: Double, type: VariableType, vararg name: String){
        val keyName = name.joinToString("_")
        val newCache = (cache["default"] ?: hashMapOf()).toMutableMap()
        newCache[keyName] = when (type) {
            VariableType.Float -> GVariable(model!!.createVar(keyName, lb, ub, 0.0, SCIP_Vartype.SCIP_VARTYPE_CONTINUOUS))
            VariableType.Integer -> GVariable(model!!.createVar(keyName, lb, ub, 0.0, SCIP_Vartype.SCIP_VARTYPE_INTEGER))
            VariableType.Binary -> GVariable(model!!.createVar(keyName, lb, ub, 0.0, SCIP_Vartype.SCIP_VARTYPE_BINARY))
        }
        cache["default"]= newCache
    }

    /**
     * 修改目标
     */
    fun setObjective(coefficient: Double, element: IVariable) {
        model!!.changeVarObj(element.getGCore(), coefficient)
    }

    override fun setObjective(constraintEq: ConstraintEq, type: ObjectiveType){
        for (i in 1..constraintEq.size()){
            model!!.changeVarObj(constraintEq.variables[i-1].getGCore(), constraintEq.coeffs[i-1])
        }
        when (type){
            ObjectiveType.Minimize -> setMinimize()
            ObjectiveType.Maximize -> setMaximize()
        }
    }

    override fun getVarByName(vararg name: String): IVariable {
        val defaultGroup = cache["default"] ?: hashMapOf()
        val keyName = name.joinToString("_")
        return defaultGroup[keyName] ?: throw InputDataValidationException("出现不存在的变量:$keyName")
    }

    override fun getVarByGroup(group: String, vararg name: String): IVariable?{
        val defaultGroup = cache[group] ?: hashMapOf()
        val keyName = name.joinToString("_")
        return defaultGroup[keyName]
    }

    fun setMinimize(){
        model!!.setMinimize()
    }

    fun setMaximize(){
        model!!.setMaximize()
    }

    override fun getObjectiveSense(): String{
        val ans = if (model!!.maximization()) {
            "Maximize"
        }else{
            "Minimize"
        }
        return ans
    }

    /**
     * 新增约束
     */
    override fun addConstr(expr: ConstraintExpr){
        val constraint = model!!.createConsLinear(expr.name,
            expr.variables.map { it.getGCore()}.toTypedArray(),
            expr.coeffs.toDoubleArray(), expr.lhs, expr.rhs)
        model!!.addCons(constraint)
        constrNum ++
        model!!.releaseCons(constraint)
    }

    override fun addConstr(expr: ConstraintEq, type: ConstraintType, side: Double, name:String){
        val constraint = when(type){
            ConstraintType.eq  -> model!!.createConsLinear(name,
                expr.variables.map { it.getGCore()}.toTypedArray(),
                expr.coeffs.toDoubleArray(), side, side)
            ConstraintType.le  -> model!!.createConsLinear(name,
                expr.variables.map { it.getGCore()}.toTypedArray(),
                expr.coeffs.toDoubleArray(), - model!!.infinity(), side)
            ConstraintType.ge  -> model!!.createConsLinear(name,
                expr.variables.map { it.getGCore()}.toTypedArray(),
                expr.coeffs.toDoubleArray(), side, model!!.infinity())
        }
        model!!.addCons(constraint)
        constrNum ++
        model!!.releaseCons(constraint)
    }

    /**
     * 求解
     */
    override fun solve() : Boolean{
        model!!.solve()
        objective = model!!.bestSol
        return true
    }

    override fun export(path: String) {
    }

    /**
     * 信息日志
     */
    override fun getStatus(): String{
        return model!!.status.toString()
    }
    override fun getNVars(): Int{
        return model!!.nVars
    }

    fun setMemory(size: Double){
        model!!.setRealParam("limits/memory", size)
    }

    fun setVerbLevel(on: Boolean){
        if (on) {
            model!!.setIntParam("display/verblevel", 1)
        }else{
            model!!.setIntParam("display/verblevel", 0)
        }
    }

    override fun tuneParameter() {
        setTimeLimit(60.0)
        setVerbLevel(false)
        setMemory(9216.0)
    }

    override fun addCallback(cb: ICallBackConstr) {
        return
    }


    override fun dispose(){
        for (v in cache.values.toList()) {
            for (vv in v.values.toList()){
                val core = vv.getGCore()
                model!!.releaseVar(core)
            }
        }
        model!!.free()
    }

    /**
     * 打印求解统计
     */
    override fun printStatistics(){
        model!!.printStatistics()
    }

    // TODO 待定
    override fun createLeRange(s: ConstraintEq, value: Double): IRange {
        return XRange()
    }

    /**
     * 获取目标函数
     */
    override fun getBestSolObj(): Double{
        var ans = 0.0
        objective?.let {
            ans = model!!.getSolOrigObj(it)
        }
        return ans
    }

    override fun getBestSolVal(variable: IVariable?): Double{
        if (variable == null){
            throw Exception("获取变量最优解失败，传入变量为空")
        }
        var ans = 0.0
        objective?.let {
            ans = model!!.getSolVal(it, variable.getGCore())
        }
        return ans
    }

    override fun getBestSolValFromName(name: String): Double {
        var variable = this.getVarByName(name)
        return this.getBestSolVal(variable)
    }

    override fun readLP(path: String){
        model!!.readProb(path)
    }

    override fun setTimeLimit(time: Double) {
        model!!.setRealParam("limits/time", time)
    }
}