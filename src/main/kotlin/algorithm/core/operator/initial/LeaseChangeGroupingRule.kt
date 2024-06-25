package core.operator.initial

import catalog.ea.ChromoseBase
import catalog.ea.TwoPartitionChromosome
import catalog.ea.CodeSequence
import problem.AbstractProblem
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class LeaseChangeGroupingRule(rd: Random) : BaseGenerationRule(rd) {
    override var name = "组序最早资源规则"
    override fun operate(problem: AbstractProblem): ChromoseBase {
        problem as JobSchedulingProblem
        // OS random generation
        val code = ArrayList<Int>()
        val groups = problem.pieceSteps.pieceCounter
        val groupLevel2 = groups.map { (ele, quantity) ->
            val mask = Util.getUniqueMaskCodeOfEntity(ele, 1)
            val realCode = problem.pieceSteps.getMaskMap()[mask]!!
            problem.pieceSteps.getPropertyMap()[realCode]!!.productGroupId to ele to quantity
        }
        for (pair in groupLevel2.groupBy { it.first }) {
            val elements = pair.value
            for (it in elements.shuffled(random)) {
                val quantity = it.second
                val ele = it.first.second
                for (i in 1..quantity) {
                    code.add(ele)
                }
            }
        }
        val offspringOS = CodeSequence(code)
        // MS rule generation
        val code1 = ArrayList<Int>()
        val jobMap = mutableMapOf<Int, Int>()
        val jobCnt = mutableMapOf<String, Double>()
        val upperBound = ArrayList<Int>()
        val initCode = mutableMapOf<String, Pair<Int, Int>>()
        for (entity in code) {
            val seq = (jobMap[entity] ?: 0) + 1
            val maskCode = Util.getUniqueMaskCodeOfEntity(entity, seq)
            val realCode = problem.pieceSteps.getMaskMap()[maskCode]!!
            val pieceStep = problem.pieceSteps.getPropertyMap()[realCode]!!
            val allowedNodes = pieceStep.getNodesByPrimaryGroup()
            var machineIndex: Int = -1
            var shortest = Double.MAX_VALUE
            for (idx in 0 until allowedNodes.size){
                val res = allowedNodes[idx]
                val compare = jobCnt[res] ?: 0.0
                if (compare < shortest) {
                    machineIndex = idx
                    shortest = compare
                }
            }
            val selectedMachine = allowedNodes[machineIndex]
            val resource = problem.resources.getPropertyMap()[selectedMachine]!!
            val eor = problem.getEntityOnResource(pieceStep, resource)
            jobCnt[selectedMachine] = (jobCnt[selectedMachine] ?: 0.0) + eor.duration
            jobMap[entity] = seq
            initCode.putIfAbsent(maskCode, machineIndex to allowedNodes.size)
        }
        for (entry in problem.pieceSteps.pieceCounter){
            val pieceCode = entry.key
            val seq = entry.value
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