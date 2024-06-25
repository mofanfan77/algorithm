package core.operator.initial

import catalog.ea.ChromoseBase
import org.example.algorithm.core.operator.Operator
import problem.AbstractProblem
import kotlin.random.Random

abstract class BaseGenerationRule : org.example.algorithm.core.operator.Operator {
    constructor(rd : Random): super(rd){
    }
    abstract fun operate(problem: AbstractProblem): ChromoseBase
}