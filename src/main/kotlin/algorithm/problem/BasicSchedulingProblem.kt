package problem

import core.entity.PlanSubResource
import utility.unicodeConnector

open class BasicSchedulingProblem : JobSchedulingProblem() {
    private var resourcePool = mutableMapOf<String, Double>()

    /**
     * 添加副资源
     */
    fun addViceResource(vices: List<PlanSubResource>) {
        this.hasViceConstraint = true
        for (sub in vices){
            this.resourcePool[sub.resourceId] = sub.quantity
        }
    }

    fun getViceResource(): MutableMap<String, Double> {
        return this.resourcePool
    }

    override fun initialize() {
        pieceStepNetwork!!.topoLogicalOrder()
        this.generatePieceStepMap()
    }


    /**
     * 生成 算法模型 -> 业务模型 code 的映射
     */
    private fun generatePieceStepMap() {
        var pieceCnt = 1
        for ((eid, stepList) in pieceStepNetwork!!.topoOrderByJob){
            for (index in stepList.indices){
                val step = stepList[index]
                step.topoIndex = index + 1
                this.pieceSteps.addMask("$pieceCnt${unicodeConnector}${index + 1}", step)
            }
            this.pieceSteps.pieceCounter[pieceCnt] = stepList.size
            this.pieceSteps.relationMap[eid] = pieceCnt
            pieceCnt += 1
        }
    }

    /**
     * 专属方法
     */
}