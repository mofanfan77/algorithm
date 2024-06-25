package core.operator.initial

import catalog.ea.ChromoseBase
import catalog.ea.TwoPartitionChromosome
import catalog.ea.CodeSequence
import problem.AbstractProblem
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class RandomJobRule(rd: Random) : BaseGenerationRule(rd){
    override var name = "随机规则"
    override fun operate(problem: AbstractProblem): ChromoseBase {
        problem as JobSchedulingProblem
        // OS random generation
        val code = ArrayList<Int>()
        for (psCode in problem.pieceSteps.getMaskMap().keys){
            val jobNr = Util.parsePieceNumberFromMaskCode(psCode)
            code.add(jobNr)
        }
        code.shuffle()
        val offspringOS = CodeSequence(code)
        // MS random generation
        val code1 = ArrayList<Int>()
        val upperBound = ArrayList<Int>()
        for ((pieceCode, seq) in problem.pieceSteps.pieceCounter){
            for (i in 1..seq) {
                val maskCode = Util.getUniqueMaskCodeOfEntity(pieceCode, i)
                val realCode = problem.pieceSteps.getMaskMap()[maskCode]!!
                val pieceStep = problem.pieceSteps.getPropertyMap()[realCode]!!
                val allowedSize = pieceStep.getNodesByPrimaryGroup().size
                val machineIndex = Random.nextInt(allowedSize)
                code1.add(machineIndex)
                upperBound.add(allowedSize)
            }
        }
        val offspringMS = CodeSequence(code1, upperBound)
        var ans = TwoPartitionChromosome(problem, offspringOS, offspringMS)
        return ans
    }
}