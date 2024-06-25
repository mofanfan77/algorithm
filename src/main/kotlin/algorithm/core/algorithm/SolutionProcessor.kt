package core.algorithm

import catalog.solution.ExploredSolution

interface SolutionProcessor {
    fun <T: ExploredSolution>evaluate(exp: T)
    fun initialEvaluate()
}