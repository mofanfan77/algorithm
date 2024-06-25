package catalog.solution

import analysis.Objective
import catalog.ea.ChromoseBase
import problem.AbstractProblem
import problem.BaseProblem

/**
 * 临时解
 */
abstract class ExploredSolution {
    var problem: AbstractProblem? = null
    var solution : AlgorithmSolution? = null
    fun getFitness(): Objective {
        return this.solution?.getFitness() ?: Objective()
    }

    operator fun compareTo(bestSolution: ChromoseBase): Int {
        return this.solution!!.getFitness().compareTo(bestSolution.solution!!.getFitness())
    }

    fun update(solution: AlgorithmSolution) {
        this.solution = solution
    }
}
