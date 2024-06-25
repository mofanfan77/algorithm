package core.operator.initial

import catalog.ea.ChromoseBase
import catalog.ea.TwoPartitionChromosome
import catalog.ea.CodeSequence
import problem.AbstractProblem
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class LeastJobGroupingRule(rd: Random): BaseGenerationRule(rd) {
    override var name = "组序最少资源规则"
    override fun operate(problem: AbstractProblem): ChromoseBase {
        problem as JobSchedulingProblem
        // OS random generation
        val code = ArrayList<Int>()
        val groups = problem.pieceSteps.pieceCounter
        for (cn in groups.keys.shuffled(random)){
            for (i in 1..groups[cn]!!){
                code.add(cn)
            }
        }
        val offspringOS = CodeSequence(code)
        // MS rule generation
        val jobMap = mutableMapOf<Int, Int>()
        val jobCnt = mutableMapOf<String, Double>()
        val code1 = ArrayList<Int>()
        val upperBound = ArrayList<Int>()
        val initCode = mutableMapOf<String, Pair<Int, Int>>()
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
            jobCnt[selectedMachine] = (jobCnt[selectedMachine]?: 0.0) + 1.0
            jobMap[entity] = seq
            initCode.putIfAbsent(maskCode, machineIndex to allowedNodes.size)
        }
        for ((pieceCode, seq) in problem.pieceSteps.pieceCounter){
            for (i in 1..seq) {
                val maskCode = Util.getUniqueMaskCodeOfEntity(pieceCode, i)
                val valuePair = initCode[maskCode]!!
                code1.add(valuePair.first)
                upperBound.add(valuePair.second)
            }
        }
        val offspringMS = CodeSequence(code1, upperBound)
        var ans = TwoPartitionChromosome(problem, offspringOS, offspringMS)
        return ans
    }
}