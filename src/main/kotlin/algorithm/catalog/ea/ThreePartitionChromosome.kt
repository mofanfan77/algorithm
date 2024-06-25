package catalog.ea

import problem.BaseProblem
import problem.JobSchedulingProblem

class ThreePartitionChromosome : ChromoseBase {
    constructor(problem: BaseProblem,
                operation: CodeSequence,
                machine: CodeSequence,
                priority: CodeSequence) {
        // 初始化编码
        when (problem) {
            // 排产场景
            is JobSchedulingProblem -> {
                this.problem = problem
                codeSequences[OS_OPERATION] = operation
                codeSequences[MS_OPERATION] = machine
                codeSequences[PT_OPERATION] = priority
                operation.owner = this
                machine.owner = this
                priority.owner = this
            }
        }
    }
}