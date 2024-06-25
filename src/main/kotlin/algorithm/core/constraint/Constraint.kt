package core.constraint

import core.entity.AttributeNetwork
import core.entity.Attributes
import core.entity.PlanEntity
import core.entity.Variable
import utility.exception.ConstrExecutionException
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

abstract class Constraint(val name: String) : Serializable {
    // 因变量（约束变量）
    lateinit var sourceAttr: Attributes
    var groupName: String = ""

    // 依赖变量
    lateinit var targetAttrs: List<Attributes>
    open lateinit var executable: ConstraintExecute
    var totalCnt = AtomicInteger()
    private var totalTime = AtomicLong()

    fun count() {
        totalCnt.addAndGet(1)
    }

    fun getCnt(): String {
        val len = this.totalCnt.toString().length
        return this.totalCnt.toString().padStart(5 - len)
    }

    fun getAvg(): String {
        return "%.5f".format(this.totalTime.toDouble().div(totalCnt.toDouble()))
    }

    fun getTime(): String {
        return "%.3f".format((this.totalTime.toLong() / 1000.0))
    }

    fun totalTime(): Double {
        return totalTime.toLong() / 1000.0
    }

    open fun calc(vars: Variable) {
        try {
            val start = System.currentTimeMillis()
            executable.compute(vars, sourceAttr, targetAttrs)
            totalTime.addAndGet(System.currentTimeMillis() - start)
        } catch (e: Exception) {
            throw ConstrExecutionException("$name - 执行方法出错, ${e.printStackTrace()}")
        }
    }

    abstract fun connect(atn: AttributeNetwork, variableList: MutableMap<PlanEntity, Variable>)
}