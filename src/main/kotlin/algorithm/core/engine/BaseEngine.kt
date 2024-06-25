package core.engine

import catalog.extensions.ConstraintType
import catalog.extensions.ObjectiveType
import catalog.extensions.RangeController
import catalog.extensions.VariableType
import catalog.extensions.callback.CallBackOperator
import catalog.extensions.callback.ICallBackConstr
import catalog.extensions.range.ConstraintEq
import catalog.extensions.range.ConstraintExpr
import catalog.extensions.variable.IVariable

abstract class BaseEngine(private var name: String) : RangeController {
    var constrNum = 0
    var cache = mutableMapOf<String, Map<String, IVariable>>()

    abstract fun addNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, vararg name: String)
    abstract fun addGroupNumVars(lb: Double, ub: Double, obj: Double, type: VariableType, groupName: String, vararg name: String)

    /**
     * 配置环境
     */
    open fun setEnv(configName: String, configValue: String){}
    open fun setEnv(configs: Map<String, String>){}

    open fun start(){}

    open fun activateCallback(name: String): Boolean{ return true}

    /**
     * 修改目标
     */
    abstract fun setObjective(constraintEq: ConstraintEq, type: ObjectiveType = ObjectiveType.Minimize)

    abstract fun getVarByName(vararg name: String): IVariable

    abstract fun getVarByGroup(group: String, vararg name: String): IVariable?

    fun geSense(): ConstraintType {
        return ConstraintType.ge
    }

    fun eqSense(): ConstraintType {
        return ConstraintType.eq
    }

    fun leSense(): ConstraintType {
        return ConstraintType.le
    }

    abstract fun getObjectiveSense(): String

    /**
     * 新增约束
     */
    abstract fun addConstr(expr: ConstraintExpr)
    abstract fun addConstr(expr: ConstraintEq, type: ConstraintType, side: Double, name: String)

    /**
     * 求解
     */
    abstract fun solve(): Boolean

    /**
     * 导出文件
     */
    abstract fun export(path: String)

    /**
     * 信息日志
     */
    abstract fun getStatus(): String
    abstract fun getNVars(): Int
    abstract fun dispose()

    /**
     * 获取目标函数
     */
    abstract fun getBestSolObj(): Double

    abstract fun getBestSolVal(variable: IVariable?): Double

    abstract fun getBestSolValFromName(name: String): Double

    abstract fun readLP(path: String)

    abstract fun setTimeLimit(time: Double)

    abstract fun tuneParameter()
    fun getNCons(): Int {
        return this.constrNum
    }

    fun linearNumExpr(): ConstraintEq {
        return ConstraintEq()
    }

    fun getName(): String {
        return name
    }

    /**
     * 指定变量
     */
    fun setVarToZero(variable: IVariable, name: String) {
        val zeroLimitConstr = this.linearNumExpr()
        zeroLimitConstr.addTerm(1.0, variable)
        this.addConstr(zeroLimitConstr, ConstraintType.le, 0.0, "upper_limit_$name")
    }

    fun setVarToZero(variable: List<IVariable>, name: String) {
        if (variable.isNotEmpty()) {
            val zeroLimitConstr = this.linearNumExpr()
            for (v in variable) {
                zeroLimitConstr.addTerm(1.0, v)
            }
            this.addConstr(zeroLimitConstr, ConstraintType.le, 0.0, "upper_limit_$name")
        }
    }

    /**
     * 回调相关
     */
    abstract fun addCallback(cb : ICallBackConstr)
    open fun addCallback(func:(String)-> Unit, name:String){}

    open fun getCallBack(name:String): CallBackOperator? {return null}

    open fun addLazyConstraint(name: String, lazyConstr: ConstraintEq, toDouble: Double){}

    open fun getCallBackValue(name:String, v: IVariable): Double{ return 0.0 }


    /**
     * 打印求解统计
     */
    abstract fun printStatistics()
}