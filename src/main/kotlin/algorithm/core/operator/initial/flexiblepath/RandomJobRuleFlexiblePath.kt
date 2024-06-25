package core.operator.initial.flexiblepath

import catalog.ea.ChromoseBase
import catalog.ea.CodeSequence
import catalog.ea.ThreePartitionChromosome
import core.operator.initial.BaseGenerationRule
import problem.AbstractProblem
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class RandomJobRuleFlexiblePath(rd: Random) : BaseGenerationRule(rd){
    override var name = "多路径下随机规则"
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
                val machineIndex = random.nextInt(allowedSize)
                code1.add(machineIndex)
                upperBound.add(allowedSize)
            }
        }
        val offspringMS = CodeSequence(code1, upperBound)
        // 优先级序列
        val code3 = ArrayList<Int>()
        for ((_, v) in problem.pieceStepNetwork!!.sameJobInbound){
            val jobStr = Util.randomTopoSort(v, random)
            for (job in jobStr) {code3.add(job.topoIndex)}
        }
        val offspringPT = CodeSequence(code3)
        var ans = ThreePartitionChromosome(problem, offspringOS, offspringMS, offspringPT)
        return ans
    }
}