package catalog.ea

import problem.BaseProblem
import problem.JobSchedulingProblem
import utility.exception.InitializationException

class MultiPartitionChromosome : ChromoseBase {
    constructor(problem: BaseProblem,
                operations: Array<CodeSequence>,
                pathFlag: Boolean) {
        if(operations.size <= 2){
            throw InitializationException("多重资源下染色体生成失败, 序列列表长度为${operations.size}")
        }
        // 初始化编码
        when (problem) {
            // 排产场景
            is JobSchedulingProblem -> {
                this.problem = problem
                var layer = if (pathFlag) PT_OPERATION else OS_OPERATION
                for (op in operations){
                    codeSequences[layer] = op
                    layer += 1
                    op.owner = this
                }
            }
        }
    }
}