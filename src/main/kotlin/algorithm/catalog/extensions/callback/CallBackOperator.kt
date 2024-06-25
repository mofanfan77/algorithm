package catalog.extensions.callback

import catalog.extensions.range.IRange
import catalog.extensions.variable.IVariable
import com.gurobi.gurobi.GRBCallback
import ilog.cplex.IloCplex

interface CallBackOperator {

    fun addLazyConstraint(lazyConstr: IRange)

    fun getCallBackValue(v: IVariable): Double

    fun getXCore(): IloCplex.LazyConstraintCallback?{ return null }

    fun getBCore(): GRBCallback?{ return null }

    fun checkStatus(): Boolean{return true}
}