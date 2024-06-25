package solver

import catalog.solution.CAPSolution
import config.CAPConfig
import core.entity.MoveEntity
import core.entity.MoveResource
import core.entity.NodeEntity
import data.dataobject.CAPResultData
import data.dataobject.CAPTraceData
import data.snapshot.CAPSnapShot
import data.snapshot.ModelSnapshot
import problem.CraneAssignProblem
import solver.datainterface.CAPInterface

abstract class CAPSolver : BaseEngineSolver(), CAPInterface {
    lateinit var problem: CraneAssignProblem
    lateinit var config: CAPConfig
    var initialSolutionFlag = false

    var timeHorizon = 60
    var safetyInterval = 5.0
    var maxLength = 1000.0
    var solution = CAPSolution()

    /**
     * 求解模型
     */
    abstract fun solveModel()

    final override fun createProblem() {
        this.problem = CraneAssignProblem()
    }

    override fun initConfig() {
        this.config = CAPConfig()
    }

    /**
     * 传入配置参数
     */
    fun setSolverConfig(config: CAPConfig) {
        this.config = config
        this.setInterval(config.safetyInterval)
        this.setTime(config.timeHorizon)
        this.maxLength = config.maxLength
    }

    fun createConfig(): CAPConfig {
        return CAPConfig()
    }

    private fun setInterval(difference: Double) {
        this.safetyInterval = difference
    }

    private fun setTime(time: Int) {
        timeHorizon = time
    }

    fun readAssignment(tasks: MutableMap<MoveResource, List<MoveEntity>>) {
        problem.readAssignment(tasks)
        initialSolutionFlag = true
    }

    /**
     * 增加任务
     */
    override fun addTasks(tasks: MutableList<MoveEntity>) {
        problem.addTasks(tasks)
    }

    /**
     * 增加车辆
     */
    override fun addCranes(resources: MutableList<MoveResource>) {
        problem.addResource(resources)
    }

    /**
     * （弃用）
     */
    override fun addRepairList(repairList: MutableList<NodeEntity>) {
        problem.repairList = repairList
    }

    /**
     * 增加工位
     */
    override fun addNodes(nodes: List<NodeEntity>) {
        problem.addNode(nodes)
    }

    // 结果处理
    override fun getCraneTrace(): List<CAPTraceData> {
        return solution.trace
    }

    override fun getAssignment(): List<CAPResultData> {
        return solution.assignment
    }

    /**
     * 导出模型数据
     */
    override fun exportData(path: String) {
        val exportion = CAPSnapShot()
        exportion.data = problem.collect()
        exportion.config = this.config
        ModelSnapshot.serialize(exportion, path)
        solverLog.info("日志文件存入成功 $path")
    }

    /**
     * 导入模型数据
     */
    override fun loadData(path: String) {
        val model = ModelSnapshot.deserialize(path) as CAPSnapShot
        this.setSolverConfig(model.config)
        this.problem.recover(model)
        solverLog.info("日志文件读取成功 $path")
    }
}