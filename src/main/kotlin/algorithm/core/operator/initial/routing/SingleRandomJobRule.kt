package core.operator.initial.routing

import catalog.ea.ChromoseBase
import catalog.ea.CodeSequence
import catalog.ea.SinglePartitionChromosome
import core.operator.initial.BaseGenerationRule
import problem.AbstractProblem
import problem.CraneAssignProblem
import kotlin.random.Random

class SingleRandomJobRule(random: Random) : BaseGenerationRule(random){
    override fun operate(problem: AbstractProblem): ChromoseBase {
        problem as CraneAssignProblem
        val ans = (1..problem.getTask().size).shuffled(random)
        val res = ArrayList<Int>()
        res.addAll(ans)
        var obj = SinglePartitionChromosome(problem, CodeSequence(res))
        return obj
    }

    override var name = "随机规则"
}