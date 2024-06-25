package catalog.ea

import problem.AbstractProblem
import problem.BaseProblem
import problem.JobSchedulingProblem

class TwoPartitionChromosome : ChromoseBase {
    //初始序列生成
    constructor(problem: AbstractProblem, operation: CodeSequence, machine: CodeSequence) {
        // 初始化编码
        when (problem) {
            // 排产场景
            is JobSchedulingProblem -> {
                this.problem = problem
                codeSequences[OS_OPERATION] = operation
                codeSequences[MS_OPERATION] = machine
                operation.owner = this
                machine.owner = this
            }
        }
    }
}
