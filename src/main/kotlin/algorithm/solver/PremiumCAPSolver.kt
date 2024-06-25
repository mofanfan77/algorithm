package solver

import catalog.extensions.ConstraintType
import core.algorithm.AlgorithmFactory
import core.engine.DefaultEngine
import core.entity.MoveEntity
import core.entity.MoveResource

class PremiumCAPSolver : BasicCAPSolver() {

    var assignResult = mutableMapOf<Int, ArrayList<Int>>()
    val scoreMatrix = mutableMapOf<Int, ArrayList<Double>>()
    override fun buildModel() {
        /**
        select and assign tasks to cranes based on objectives
        assignTasks()
        optimize trace based on assignments
        this.readAssignment()
         */
        this.assignTasks()
        this.optimizeTrace()
    }

    private fun measureScore(entity: MoveEntity, resource: MoveResource): Double {
        return 0.0
    }

    private fun generateScores() {
        this.problem.calcDistance()
        for (i in 1..this.problem.getTask().size) {
            scoreMatrix[i] = arrayListOf()
            for (j in 1..this.problem.getResource().size) {
                val score = measureScore(problem.getTask()[i - 1], problem.getResource()[j - 1])
                scoreMatrix[i]!!.add(score)
            }
        }
    }

    private fun optimizeAssignments() {
        val algo = AlgorithmFactory.createSimpleHybridAlgorithm(this)
        algo.start()
    }

    /**
     * task assignment
     */
    private fun assignTasks() {
        // 边权累计得分
        // 边权二维矩阵 key=taskIndex
        this.generateScores()
        // assignment 寻优
        this.optimizeAssignments()
    }

    override fun addSpecialConstraints(model: DefaultEngine) {
        // 根据传入的assignResult 把其他的O组合剔除
        for (j in 1..problem.getResource().size) {
            val assignedList = assignResult[j] ?: arrayListOf()
            val newConstr = model.linearNumExpr()
            for (i in 1..problem.getTask().size) {
                if (i !in assignedList) {
                    for (t in 1..timeHorizon) {
                        val keyName = "O_${i}_${j}_${t}"
                        newConstr.addTerm(1.0, model.getVarByName(keyName))
                    }
                }
            }
            model.addConstr(newConstr, ConstraintType.le, 0.0, "banned_${j}")
        }
        // 根据先后顺序
        for ((j, tasks) in assignResult) {
            for (idx in 1 until tasks.size) {
                val newConstraint = model.linearNumExpr()
                for (t in 1..timeHorizon) {
                    val keyName1 = "O_${tasks[idx]}_${j}_${t}"
                    val keyName2 = "F_${tasks[idx]}_${j}_${t}"
                    newConstraint.addTerm(t.toDouble(), model.getVarByName(keyName1))
                    newConstraint.addTerm(t.toDouble(), model.getVarByName(keyName2))
                }
                model.addConstr(newConstraint, ConstraintType.ge, 0.0, "resource_order_${j}")
            }
        }
    }

    /**
     * optimize trace
     */
    private fun optimizeTrace() {
        this.buildCraneAssignmentProblem()
    }
}