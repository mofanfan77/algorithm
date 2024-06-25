package catalog.extensions.callback

import catalog.extensions.range.ConstraintEq
import catalog.extensions.variable.IVariable
import core.engine.BaseEngine
import ilog.concert.IloRange
import ilog.cplex.IloCplex

abstract class CXCallBackConstr(private val model: BaseEngine) : IloCplex.LazyConstraintCallback(), ICallBackConstr {

    fun getValue(v: IVariable): Double {
        return this.getValue(v.getXCore())
    }

    override fun getCoreC():IloCplex.LazyConstraintCallback { return this}

    fun add(s: ConstraintEq, value: Double) {
        var cs = model.createLeRange(s, value)
        this.add(cs.getXCore())
    }
}
