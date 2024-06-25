package core.operator.initial.routing

import catalog.ea.ChromoseBase
import catalog.ea.CodeSequence
import catalog.ea.SinglePartitionChromosome
import core.operator.initial.BaseGenerationRule
import problem.AbstractProblem
import problem.CraneAssignProblem
import kotlin.random.Random

class SingleBestJobRule(random: Random) : BaseGenerationRule(random){
    override fun operate(problem: AbstractProblem): ChromoseBase {
        problem as CraneAssignProblem
        val ans = (1..problem.getTask().size).shuffled(random)
        val visited = HashSet<Int>()
        val res = ArrayList<Int>()
        var last = ans[0]
        res.add(last)
        for (k in 1 until problem.getTask().size){
            val selected = problem.findClosest(last, visited)
            visited.add(selected)
            res.add(selected)
            last = selected
        }
        var obj = SinglePartitionChromosome(problem, CodeSequence(res))
        return obj
    }

    override var name = "最近规则"
}