package core.operator.crossover

import org.example.algorithm.core.operator.Operator
import catalog.ea.CodeSequence
import kotlin.random.Random

abstract class CrossOverOperator : org.example.algorithm.core.operator.Operator {
    constructor()
    constructor(rd: Random) : super(rd) {
    }

    open fun run(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence> {
        return if (father.getSize() > 0 && mother.getSize() > 0) {
            operate(father, mother)
        } else {
            ArrayList()
        }
    }

    abstract fun operate(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence>
}