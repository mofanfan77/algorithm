package core.operator.initial.multiresource

import catalog.ea.ChromoseBase
import catalog.ea.CodeSequence
import catalog.ea.MultiPartitionChromosome
import core.operator.initial.BaseGenerationRule
import problem.AbstractProblem
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class RandomJobRuleMultipleResource(random: Random, private val layer: Int, private val pathFlag: Boolean) : BaseGenerationRule(random){
    override var name = "多重资源下的随机规则"

    override fun operate(problem: AbstractProblem): ChromoseBase {
        problem as JobSchedulingProblem
        val codeSeq = ArrayList<CodeSequence>()
        // 优先级序列
        if (pathFlag) {
            val code3 = ArrayList<Int>()
            for ((_, v) in problem.pieceStepNetwork!!.sameJobInbound) {
                val jobStr = Util.randomTopoSort(v, random)
                for (job in jobStr) {
                    code3.add(job.topoIndex)
                }
            }
            codeSeq.add(CodeSequence(code3))
        }
        // OS random generation
        val code = ArrayList<Int>()
        for (psCode in problem.pieceSteps.getMaskMap().keys){
            val jobNr = Util.parsePieceNumberFromMaskCode(psCode)
            code.add(jobNr)
        }
        code.shuffle()
        codeSeq.add(CodeSequence(code))
        // MS random generation
        for (l in 1..layer){
            val code1 = ArrayList<Int>()
            val upperBound = ArrayList<Int>()
            for ((pieceCode, seq) in problem.pieceSteps.pieceCounter){
                for (i in 1..seq) {
                    val maskCode = Util.getUniqueMaskCodeOfEntity(pieceCode, i)
                    val realCode = problem.pieceSteps.getMaskMap()[maskCode]!!
                    val pieceStep = problem.pieceSteps.getPropertyMap()[realCode]!!
                    val availNodes = pieceStep.getNodesByGroupIndex(l-1)
                    val allowedSize = availNodes.size
                    val machineIndex = random.nextInt(allowedSize)
                    code1.add(machineIndex)
                    upperBound.add(allowedSize)
                }
            }
            codeSeq.add(CodeSequence(code1, upperBound))
        }
        var ans = MultiPartitionChromosome(problem, codeSeq.toTypedArray(), pathFlag)
        return ans
    }
}