package catalog.extensions.range

import catalog.extensions.variable.IVariable

open class ConstraintEq {
    var variables = arrayListOf<IVariable>()
    var coeffs = arrayListOf<Double>()

    fun size(): Int{
        return variables.size
    }

    fun addTerm(coeff: Double, variable: IVariable?){
        if (coeff != 0.0) {
            variable?.let {
                variables.add(it)
                coeffs.add(coeff)
            }
        }
    }

    operator fun plus(other: ConstraintEq): ConstraintEq {
        variables.addAll(other.variables)
        coeffs.addAll(other.coeffs)
        return this
    }

    operator fun minus(other: ConstraintEq): ConstraintEq {
        variables.addAll(other.variables)
        coeffs.addAll(other.coeffs.map { it * -1.0 })
        return this
    }

    operator fun minus(other: IVariable): ConstraintEq {
        variables.add(other)
        coeffs.add(-1.0)
        return this
    }

    companion object {
        fun sumOf(variables: ArrayList<IVariable>, coeff: ArrayList<Double>): ConstraintEq {
            val new = ConstraintEq()
            new.coeffs = coeff
            new.variables = variables
            return new
        }
    }
}
