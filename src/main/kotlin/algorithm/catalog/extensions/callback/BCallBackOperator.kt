package catalog.extensions.callback

import catalog.extensions.ConstraintType
import catalog.extensions.range.IRange
import catalog.extensions.variable.IVariable
import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBCallback

class BCallBackOperator(var func: (String) -> Unit, var name: String) : CallBackOperator, GRBCallback() {

    override fun checkStatus(): Boolean {
        return where == GRB.CB_MIPSOL
    }

    override fun addLazyConstraint(lazyConstr: IRange) {
        val value = lazyConstr.getValue()
        val sense = lazyConstr.getSense()
        when (sense) {
            ConstraintType.le -> this.addLazy(lazyConstr.getBCore(), GRB.LESS_EQUAL, value)
            else -> return
        }

    }

    override fun getCallBackValue(v: IVariable): Double {
        return this.getSolution(v.getBCore())
    }

    override fun getBCore(): GRBCallback {
        return this
    }

    override fun callback() {
        func(name)
    }

}