package core.operator.crossover

import catalog.ea.CodeSequence
import problem.JobSchedulingProblem
import kotlin.random.Random

class PathSegmentCrossOver : CrossOverOperator(){
    override var name = "多分段-定点交叉算子"

    override fun operate(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence> {
        val offspring1 = ArrayList<Int>(father.getSize())
        val offspring2 = ArrayList<Int>(mother.getSize())
        val orig1 = father.generateCodeArray()
        val orig2 = mother.generateCodeArray()
        var start = 0
        val problem = father.owner?.problem!! as JobSchedulingProblem
        for ((_,v) in problem.pieceSteps.pieceCounter){
            val anchorPoint = random.nextInt(v)
            val cut1 = orig1.subList(start, start + anchorPoint + 1)
            val cut2 = orig2.subList(start, start + anchorPoint + 1)
            offspring1.addAll(cut1)
            offspring2.addAll(cut2)
            for (i in 0 until v){
                if (orig2[start + i] !in cut1){
                    offspring1.add(orig2[start + i])
                }
                if (orig1[start + i] !in cut2){
                    offspring2.add(orig1[start + i])
                }
            }
            start += v
        }
        return arrayListOf(CodeSequence(offspring1), CodeSequence(offspring2))
    }
}