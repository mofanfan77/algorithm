package core.operator.mutation

import catalog.ea.CodeSequence
import catalog.solution.FJSPSolution
import problem.JobSchedulingProblem
import kotlin.random.Random

class ShiftMutation(rd: Random): MutationOpeator(rd){
    override var name = "MS插入变异算子"
    private var mutationRatio = 0.1

    override fun operate(father: CodeSequence) : CodeSequence {
        val sol = father.owner?.solution!! as FJSPSolution
        val busy = sol.getEntitiesFromResource(sol.getLatestResource(random))
        val offspring = father.generateCodeArray()
        for (selected in busy){
            if (random.nextDouble() > mutationRatio) {
                val index = (father.owner?.problem!! as JobSchedulingProblem).getIndexOfEntity(selected)
                val upperSize = father.getUB(index)
                val newMachine = random.nextInt(upperSize)
                offspring[index] = newMachine
            }
        }
        return CodeSequence(offspring, father.generateUBArray())
    }
}