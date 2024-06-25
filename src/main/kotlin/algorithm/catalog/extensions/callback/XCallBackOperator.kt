package catalog.extensions.callback

import catalog.extensions.range.IRange
import catalog.extensions.variable.IVariable
import com.gurobi.gurobi.GRBCallback
import ilog.cplex.IloCplex
import utility.annotation.Ignored

class XCallBackOperator(var func: (String) -> Unit, var name: String) : CallBackOperator, IloCplex.LazyConstraintCallback() {


    override fun addLazyConstraint(lazyConstr: IRange) {
        this.add(lazyConstr.getXCore())
    }

    override fun getCallBackValue(v: IVariable): Double {
        return this.getValue(v.getXCore())
    }

    override fun getXCore(): IloCplex.LazyConstraintCallback{
        return this
    }

    override fun getBCore(): GRBCallback? {
        return null
    }

    override fun main() {
        func(name)
    }

}