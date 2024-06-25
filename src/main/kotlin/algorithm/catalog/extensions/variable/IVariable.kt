package catalog.extensions.variable

import catalog.extensions.range.ConstraintEq
import com.gurobi.gurobi.GRBVar
import ilog.concert.IloNumVar
import jscip.Variable

abstract class IVariable{
    open fun getGCore() : Variable?{ return null }
    open fun getXCore() : IloNumVar?{ return null }

    open fun getBCore(): GRBVar? {return  null}

    operator fun plus(other : IVariable) : ConstraintEq {
        val new = ConstraintEq()
        new.variables.add(this)
        new.variables.add(other)
        new.coeffs.add(1.0)
        new.coeffs.add(1.0)
        return new
    }

    operator fun minus(other : IVariable) : ConstraintEq {
        val new = ConstraintEq()
        new.variables.add(this)
        new.variables.add(other)
        new.coeffs.add(1.0)
        new.coeffs.add(-1.0)
        return new
    }

    operator fun times(other : Double) : ConstraintEq {
        val new = ConstraintEq()
        new.variables.add(this)
        new.coeffs.add(other)
        return new
    }
}