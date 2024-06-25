package catalog.component

import catalog.solution.AlgorithmSolution
import data.output.Output
import problem.BaseProblem

class SolverManager (var problem: BaseProblem){
    private var record = mutableMapOf<Int, Double>()

    fun parseSolution(solution: AlgorithmSolution?): Output {
        require(solution != null) { "未找到可行解" }
        solution.log()
        return solution.convertToOutput(problem)
    }
}