package catalog.extensions.range

class ConstraintExpr(var name: String): ConstraintEq() {
    var lhs = 0.0
    var rhs = 0.0

    fun add(constr: ConstraintEq){
        this.variables.addAll(constr.variables)
        this.coeffs.addAll(constr.coeffs)
    }
}
