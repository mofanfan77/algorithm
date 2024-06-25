package core.operator.crossover

import catalog.ea.CodeSequence
import problem.JobSchedulingProblem
import kotlin.random.Random

class PriorityExchangeCrossOver(rd: Random) : CrossOverOperator(rd){
    override var name = "多分段-整体交叉算子"

    override fun operate(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence> {
        val offspring1 = ArrayList<Int>(father.getSize())
        val offspring2 = ArrayList<Int>(mother.getSize())
        val orig1 = father.generateCodeArray()
        val orig2 = mother.generateCodeArray()
        var start = 0
        val problem = father.owner?.problem!! as JobSchedulingProblem
        for ((_,v) in problem.pieceSteps.pieceCounter){
            val prob = random.nextDouble()
            if (prob > 0.5){
                offspring1.addAll(orig1.subList(start, start + v))
                offspring2.addAll(orig2.subList(start, start + v))
            }else{
                offspring1.addAll(orig2.subList(start, start + v))
                offspring2.addAll(orig1.subList(start, start + v))
            }
            start += v
        }
        return arrayListOf(CodeSequence(offspring1), CodeSequence(offspring2))
    }
}