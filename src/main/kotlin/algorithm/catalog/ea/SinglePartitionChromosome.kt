package catalog.ea

import problem.AbstractProblem

class SinglePartitionChromosome : ChromoseBase {
    constructor(problem: AbstractProblem, operation: CodeSequence) {
        // 初始化编码
        this.problem = problem
        codeSequences[OS_OPERATION] = operation
        operation.owner = this
    }
}