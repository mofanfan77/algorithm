package core.operator.mutation

import catalog.ea.CodeSequence
import problem.JobSchedulingProblem
import utility.Util
import kotlin.random.Random

class RandomSwitchInGivenMutation(random: Random): MutationOpeator(random){
    override var name = "多路径场景-前置交换算子"

    override fun operate(father: CodeSequence): CodeSequence {
        val problem = father.owner?.problem!! as JobSchedulingProblem
        val codeList = ArrayList<Int>()
        val offspring = father.generateCodeArray()
        var start = 0
        var end = 0
        for ((k,v) in problem.pieceSteps.pieceCounter){
            end = start + v
            val randomNumberMoveToLast = random.nextInt(start, end)
            val targetCode = offspring[randomNumberMoveToLast]
            val entityCode = Util.getUniqueMaskCodeOfEntity(k, targetCode)
            val entity = problem.getEntityFromMask(entityCode)
            var left = 0
            var right = v
            val leftIntv = problem.pieceStepNetwork?.getPrevEntity(entity) ?: arrayListOf()
            for (l in leftIntv){
                left = maxOf(l.topoIndex, left)
            }
            val rightIntv = problem.pieceStepNetwork?.getAfterEntity(entity) ?: arrayListOf()
            for (r in rightIntv){
                right = minOf(r.topoIndex, right)
            }
            val finalPosition = if (right > left ) random.nextInt(left, right) + start else randomNumberMoveToLast
            if (randomNumberMoveToLast < finalPosition){ // 数组右移
                for (j in start until randomNumberMoveToLast){
                    codeList.add(offspring[j])
                }
                for (j in randomNumberMoveToLast + 1 until finalPosition){
                    codeList.add(offspring[j])
                }
                codeList.add(offspring[randomNumberMoveToLast])
                for (j in finalPosition until end){
                    codeList.add(offspring[j])
                }
            }else if (randomNumberMoveToLast > finalPosition){
                for (j in start until finalPosition){
                    codeList.add(offspring[j])
                }
                codeList.add(offspring[randomNumberMoveToLast])
                for (j in finalPosition  until randomNumberMoveToLast){
                    codeList.add(offspring[j])
                }
                for (j in randomNumberMoveToLast+1  until end){
                    codeList.add(offspring[j])
                }
            } else {
                for (j in start until end){
                    codeList.add(offspring[j])
                }
            }
            start += v
        }
        return CodeSequence(codeList)
    }
}