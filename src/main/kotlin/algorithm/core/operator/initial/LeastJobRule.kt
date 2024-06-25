package core.operator.initial

import catalog.ea.ChromoseBase
import catalog.ea.TwoPartitionChromosome
import catalog.ea.CodeSequence
import problem.AbstractProblem
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class LeastJobRule(rd: Random) : BaseGenerationRule(rd) {
    override var name = "工件最早资源规则"
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
        // MS rule generation
        val initCode = mutableMapOf<String, Pair<Int, Int>>()
        val jobMap = mutableMapOf<Int, Int>()
        val jobCnt = mutableMapOf<String, Double>()
        for (entity in code){
            val seq = (jobMap[entity] ?: 0) + 1
            val maskCode = Util.getUniqueMaskCodeOfEntity(entity, seq)
            val realCode = problem.pieceSteps.getMaskMap()[maskCode]!!
            val pieceStep = problem.pieceSteps.getPropertyMap()[realCode]!!
            val allowedNodes = pieceStep.getNodesByPrimaryGroup()
            var machineIndex : Int = -1
            var shortest = Double.MAX_VALUE
            for (idx in allowedNodes.indices){
                val res = allowedNodes[idx]
                val compare = jobCnt[res]?: 0.0
                if (compare < shortest ){
                    machineIndex = idx
                    shortest = compare
                }
            }
            val selectedMachine = allowedNodes[machineIndex]
            jobMap[entity] = seq
            jobCnt[selectedMachine] = (jobCnt[selectedMachine]?: 0.0) + 1.0
            initCode.putIfAbsent(maskCode, machineIndex to allowedNodes.size)
        }
        val msCode= ArrayList<Int>()
        val ubCode = ArrayList<Int>()
        for ((pieceCode, seq) in problem.pieceSteps.pieceCounter) {
            for (i in 1..seq) {
                val maskCode = Util.getUniqueMaskCodeOfEntity(pieceCode, i)
                val valuePair = initCode[maskCode]!!
                msCode.add(valuePair.first)
                ubCode.add(valuePair.second)
            }
        }
        val offspringMS = CodeSequence(msCode, ubCode)
        var ans = TwoPartitionChromosome(problem, offspringOS, offspringMS)
        return ans
    }
}