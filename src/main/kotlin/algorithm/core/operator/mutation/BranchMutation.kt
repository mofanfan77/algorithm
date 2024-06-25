package core.operator.mutation

import catalog.ea.CodeSequence
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class BranchMutation(rd: Random): MutationOpeator(rd){
    override var name = "多路径场景-分支路径变异算子"

    override fun operate(father: CodeSequence): CodeSequence {
        val problem = father.owner?.problem!! as JobSchedulingProblem
        val codeList = ArrayList<Int>()
        val offspring = father.generateCodeArray()
        var start = 0
        var end = 0
        for ((k, v) in problem.pieceStepNetwork!!.sameJobInbound){
            val size = problem.pieceStepNetwork!!.topoOrderByJob[k]?.size ?: 0
            end = start + size
            if (random.nextDouble() > 0.5) {
                val jobList = Util.topoDFS(v)
                for (job in jobList) {
                    codeList.add(job.topoIndex)
                }
            }else{
                codeList.addAll(offspring.subList(start, end))
            }
            start = end
        }
        return CodeSequence(codeList)
    }
}