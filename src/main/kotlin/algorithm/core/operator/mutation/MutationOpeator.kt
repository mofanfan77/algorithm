package core.operator.mutation

import org.example.algorithm.core.operator.Operator
import catalog.ea.CodeSequence
import kotlin.random.Random

abstract class MutationOpeator: org.example.algorithm.core.operator.Operator {
    constructor(rd : Random): super(rd){
    }
    fun run(father: CodeSequence): CodeSequence?{
        return if (father.getSize() > 0){
            operate(father)
        }else{
            null
        }
    }
    abstract fun operate(father: CodeSequence) : CodeSequence
}