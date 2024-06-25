package core.operator.mutation

import catalog.ea.CodeSequence
import problem.JobSchedulingProblem
import kotlin.random.Random

class MoveToLastPathMutation(random: Random): MutationOpeator(random){
    override var name = "多路径场景-末置交换算子"

    override fun operate(father: CodeSequence): CodeSequence {
        val problem = father.owner?.problem!! as JobSchedulingProblem
        val codeList = ArrayList<Int>()
        val offspring = father.generateCodeArray()
        var start = 0
        var end = 0
        for ((k,v) in problem.pieceSteps.pieceCounter){
            end = start + v
            val randomNumberMoveToLast = random.nextInt(start, end)
            codeList.add(offspring[randomNumberMoveToLast])
            for (j in start until end){
                if (j != randomNumberMoveToLast){
                    codeList.add(offspring[j])
                }
            }
            start += v
        }
        return CodeSequence(codeList)
    }
}