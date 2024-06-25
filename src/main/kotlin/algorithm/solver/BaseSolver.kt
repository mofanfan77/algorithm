package solver

import config.AlgorithmConfig
import core.algorithm.AlgorithmInterface
import core.entity.Attributes
import core.entity.Variable
import data.BaseData
import data.output.Output
import problem.BaseProblem
import utility.enums.AttrClassEnum
import utility.enums.AttrTypeEnum
import utility.enums.ObjectiveEnum
import utility.enums.ParameterEnum
import utility.exception.ObjectiveLevelToleranceError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import solver.datainterface.SnapShotInterface


val solverLog: Logger = LoggerFactory.getLogger(BaseSolver::class.java)

abstract class BaseSolver(private var problem: BaseProblem) : SnapShotInterface {
    private var solution: Output? = null
    private var isInitialized = false

    // 求解器参数
    lateinit var algorithmParameter: AlgorithmConfig
    lateinit var algorithm: AlgorithmInterface
    abstract fun loadAlgorithm()

    /**
     * 参数校验等流程
     */
    abstract fun validate()
    abstract fun solve()

    fun setParameter(parameter: ParameterEnum, value: Any) {
        algorithmParameter.set(parameter, value)
    }

    /**
     * 添加变量
     *
     */
    @Deprecated("变量名已舍弃")
    fun addVariable(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) {
        problem.addVariable(name, desc, type, group)
    }

    fun addVariableGroup(name: String, vararg attributes: String) {
        problem.addVariableGroup(name, *attributes)
    }

    fun addVariable(name: String, group: AttrClassEnum) {
        problem.addVariable(name, "", AttrTypeEnum.Calculation, group)
    }

    fun addVariables(names: List<String>, group: AttrClassEnum) {
        for (name in names) {
            problem.addVariable(name, "", AttrTypeEnum.Calculation, group)
        }
    }

    /**
     * 添加参数
     */
    fun addParameter(name: String, group: AttrClassEnum) {
        problem.addVariable(name, "", AttrTypeEnum.Constant, group)
    }

    fun addParameters(names: List<String>, group: AttrClassEnum) {
        for (name in names) {
            problem.addVariable(name, "", AttrTypeEnum.Constant, group)
        }
    }

    /**
     * 获取变量
     */
    fun mapAttribute(name: String): Attributes {
        return this.problem.generator.mapAttribute(name)
    }

    /**
     * 添加目标函数 - 默认接口
     */
    fun addObjective(name: String,
                     formula: (List<Variable>) -> Double,
                     weight: Double,
                     type: ObjectiveEnum
    ) {
        problem.addObjective(name, formula, weight, type)
    }

    /**
     * 添加目标函数 - 分层优化接口
     */
    fun addObjective(name: String,
                     formula: (List<Variable>) -> Double,
                     level: Int,
                     weight: Double,
                     type: ObjectiveEnum) {
        problem.addObjective(name, formula, weight, type, level)
    }

    /**
     * 针对分层优化， 设置不同层级下的容忍阈值
     */
    fun setObjectiveTolerance(level: Int, relativeTolerance: Double) {
        if (relativeTolerance < 0 || relativeTolerance > 1.0) {
            throw ObjectiveLevelToleranceError("设置的容忍系数超过范围, 层级${level} - $relativeTolerance")
        }
        problem.setObjectiveTolerance(level, relativeTolerance)
    }


    fun result(): List<BaseData> {
        return solution?.solution ?: arrayListOf()
    }

    fun initialize() {
        if (!isInitialized) {
            solverLog.info("Solver initialization start..")
            problem.initialize()
            this.loadAlgorithm()
            solverLog.info("Solver initialization end..")
            this.isInitialized = true
        }
    }

    fun updateSolution(solution: Output) {
        this.solution = solution
        solverLog.info("Output recording end..")
    }
}