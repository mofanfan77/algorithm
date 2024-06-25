package core.operator

import catalog.ea.ChromoseBase
import catalog.ea.Population
import org.example.algorithm.core.operator.Operator
import kotlin.random.Random

abstract class SelectionOperator : Operator {
    constructor(rd : Random): super(rd){
    }
    open fun operate(pop: Population) {}
}


class TournamentSelection(rd: Random) : SelectionOperator(rd) {
    override var name = "锦标赛选择算子"

    override fun operate(pop: Population) {
        val popSize = pop.getCapacity()
        val children = ArrayList<ChromoseBase>()
        for (i in 1..popSize) {
            val idx1 = Random.nextInt(popSize)
            val idx2 = Random.nextInt(popSize)
            if (pop[idx1].getFitness() > pop[idx2].getFitness()) {
                children.add(pop[idx1])
            } else {
                children.add(pop[idx2])
            }
        }
        pop.reset()
        pop.addAll(children)
    }
}