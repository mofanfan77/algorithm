package catalog.extensions.range

import catalog.extensions.ConstraintType
import com.gurobi.gurobi.GRBLinExpr
import utility.annotation.Ignored
import ilog.concert.IloRange
@Ignored
interface IRange {

    // 方法需要标注
    fun getXCore(): IloRange?{ return null}

    fun getBCore(): GRBLinExpr?{ return null}

    fun getSense(): ConstraintType

    fun getValue(): Double
}