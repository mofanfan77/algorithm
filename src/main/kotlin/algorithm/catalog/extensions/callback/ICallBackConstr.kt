package catalog.extensions.callback

import ilog.cplex.IloCplex

interface ICallBackConstr {
    fun getCoreC(): IloCplex.LazyConstraintCallback?
}