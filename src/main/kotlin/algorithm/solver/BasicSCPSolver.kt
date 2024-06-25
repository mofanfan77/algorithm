package solver

import catalog.extensions.ConstraintType
import catalog.extensions.ObjectiveType
import catalog.extensions.VariableType
import config.SCPConfig
import core.engine.BaseEngine
import core.entity.supplychain.*
import org.slf4j.LoggerFactory
import problem.SupplyChainProblem
import utility.Algorithms

/**
 * 标准供应链场景求解
 */
class BasicSCPSolver : BaseEngineSolver() {
    private lateinit var problem: SupplyChainProblem
    private lateinit var engine: BaseEngine
    private lateinit var config: SCPConfig

    private val solverLog = LoggerFactory.getLogger(BasicSCPSolver::class.java)
    init {
        createEngine()
        createProblem()
        initConfig()
    }

    fun setConfig(config: SCPConfig) {
        this.config = config
    }

    /**
     * 求解
     */
    fun build() {
        createVariables()
        createConstraints()
        createObjectives()
    }

    fun solve() {
        // 默认参数
        engine.tuneParameter()
        // 求解时间
        engine.setTimeLimit(config.timeLimit)
        var solveFlag = engine.solve()
        solverLog.info("best objective is ${engine.getBestSolObj()}")
        this.logout(engine)
        engine.dispose()
    }

    private fun logout(model: BaseEngine) {
        var path = ".\\src\\test\\resources\\output\\result_SCP.lp"
        try {
            model.export(path)
        } catch (e: Exception) {
            solverLog.warn("fail to print log to ${path} ___ 日志打印失败,")
        }
    }

    /**
     * 添加变量
     */
    private fun createVariables() {
        // x_it product i at time t at machine m (related to machine)
        for (t in 1..problem.period) {
            for ((_, steps) in problem.getProductRouteStepUnits()) {
                for (step in steps) {
                    if (step.outputFlag) {
                        // 输出物料
                        val varName1 = "Export_${step.uniqueId}_${t}"
                        // 后处理
                        val varName2 = "Used_${step.uniqueId}_${t}"
                        // 处理中
                        val varName3 = "Await_${step.uniqueId}_${t}"
                        engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName1)
                        engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName2)
                        engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName3)
                    } else {
                        // 输入物料
                        val varName = "import_${step.uniqueId}_${t}"
                        engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName)
                    }
                }
            }
        }
        // INV_it product i at time t (regardless of machine)
        for (t in 1..problem.period) {
            for ((mat, _) in problem.getProducts()) {
                // 成品物料
                val varName0 = "finished_${mat}_${t}"
                // 库存
                val varName = "inventory_${mat}_${t}"
                // 履约物料
                val varName3 = "fulfill_${mat}_${t}"
                // 采购物料
//                val varName4 = "purchase_${mat}_${t}"
                engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName)
                engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName0)
                engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName3)
//                engine.addNumVars(0.0, Double.MAX_VALUE, 0.0, VariableType.Float, varName4)
            }
        }
    }

    private fun createConstraints() {
        addCapacityConstraints()
        addMaterialBalanceConstraints()
        addWorkInProcessConstraints()
        addInventoryBalanceConstraints()
        addFulfillConstraints()
    }

    /**
     * 履约约束
     */
    private fun addFulfillConstraints() {
        for (t in 1..problem.period) {
            for ((mat, _) in problem.getProducts()) {
                var constr = engine.linearNumExpr()
                val varName3 = "fulfill_${mat}_${t}"
                constr.addTerm(1.0, engine.getVarByName(varName3))
                val key2 = "${mat}_$t"
                val order = problem.getProductOrders()[key2] ?: arrayListOf()
                var totalDemand = 0.0
                if (order.isEmpty()) {
                    continue
                } else {
                    for (od in order) {
                        totalDemand += od.quantity
                    }
                }
                engine.addConstr(constr, ConstraintType.le, totalDemand, "fulfill_demand_${mat}_${t}")
            }
        }
    }

    /**
     * WIP 约束
     * 考虑处理时间, 处理finish和 export的关系
     */
    private fun addWorkInProcessConstraints() {
        for (t in 1..problem.period) {
            for ((i, lis) in problem.getProducts()) {
                val aggVar = "finished_${i}_${t}"
                val finishConstr = engine.linearNumExpr()
                finishConstr.addTerm(1.0, engine.getVarByName(aggVar))
                for (step in lis) {
                    if (!step.outputFlag) { continue}
                    val duration = step.durationTime
                    val mainPeriod = duration.toInt() + 1
                    val partialPeriod = mainPeriod - duration
                    val varName2 = "Used_${step.uniqueId}_${t}"
                    val varName3 = "Export_${step.uniqueId}_${t}"
                    val varName4 = "Await_${step.uniqueId}_${t}"
                    val usedConstr = engine.linearNumExpr()
                    val unitId = step.unitId
                    usedConstr.addTerm(1.0, engine.getVarByName(varName2))
                    engine.addConstr(usedConstr, ConstraintType.le,
                        partialPeriod * problem.getUnit(unitId).capacity,"constr_less_remain_$varName2")
                    engine.addConstr(engine.getVarByName(varName2) - engine.getVarByName(varName3),
                        ConstraintType.le, 0.0, "constr_less_export_$varName2")
                    engine.addConstr(engine.getVarByName(varName3) - engine.getVarByName(varName2) - engine.getVarByName(varName4),
                        ConstraintType.eq, 0.0, "constr_equation_$varName2")
                    if (t > mainPeriod){
                        val var1 = "Await_${step.uniqueId}_${t - mainPeriod}"
                        finishConstr.addTerm(-1.0, engine.getVarByName(var1))
                    }
                    if (t >=  mainPeriod) {
                        val var2 = "Used_${step.uniqueId}_${t - mainPeriod + 1}"
                        finishConstr.addTerm(-1.0, engine.getVarByName(var2))
                    }
                }
                engine.addConstr(finishConstr, ConstraintType.eq, 0.0, "constr_$aggVar")
            }
        }
    }

    /**
     * 产能约束 (仅考虑投料当天的产能占用)
     */
    private fun addCapacityConstraints() {
        for (t in 1..problem.period) {
            for ((j, steps) in problem.getProductByUnits()) {
                var capacityExpr = engine.linearNumExpr()
                for (step in steps) {
                    if (!step.outputFlag) {
                        val varName = "import_${step.uniqueId}_${t}"
                        capacityExpr.addTerm(1.0, engine.getVarByName(varName))
                    }
                }
                engine.addConstr(capacityExpr, ConstraintType.le, problem.getUnit(j).capacity, "capacity_${j}_${t}")
            }
        }
    }

    /**
     * 库存平衡约束
     */
    private fun addInventoryBalanceConstraints() {
        for (t in 1..problem.period) {
            for ((mat, lis) in problem.getProducts()) {
                var materialBalanceConstr = engine.linearNumExpr()
                var name = "inventory_${mat}_${t}"
                var n3 = "fulfill_${mat}_${t}"
                var n1 = "finished_${mat}_${t}"
                for (l in lis) {
                    if (!l.outputFlag) {
                        val n1 = "import_${l.uniqueId}_${t}"
                        materialBalanceConstr.addTerm(-1.0, engine.getVarByName(n1))
                    }
                }
                materialBalanceConstr.addTerm(1.0, engine.getVarByName(n1))
                if (t > 1) {
                    var name2 = "inventory_${mat}_${t - 1}"
                    materialBalanceConstr.addTerm(1.0, engine.getVarByName(name2))
                }
                materialBalanceConstr.addTerm(-1.0, engine.getVarByName(n3))
                materialBalanceConstr.addTerm(-1.0, engine.getVarByName(name))
                engine.addConstr(materialBalanceConstr, ConstraintType.eq, 0.0, "inv_balance_${mat}_$t")
            }
        }
    }

    /**
     * 不同物料流入流出平衡约束
     */
    private fun addMaterialBalanceConstraints() {
        for (t in 2..problem.period) {
            for ((mat, steps) in problem.getProductRouteStepUnits()) {
                val varList = arrayListOf<String>()
                val coefList = arrayListOf<Double>()
                val firstStep = steps.first()
                for (step in steps) {
                    val n2: String
                    if (step.outputFlag) { // 输出
                        n2 = "Export_${firstStep.uniqueId}_${t}"
                    } else { // 输入
                        n2 = "import_${firstStep.uniqueId}_${t}"
                    }
                    varList.add(n2)
                    coefList.add(step.ratio)
                }
                if (varList.size > 1) {
                    for (idx in 1 until varList.size) {
                        var materialBalance = engine.linearNumExpr()
                        materialBalance.addTerm(coefList[0], engine.getVarByName(varList[0]))
                        materialBalance.addTerm(-coefList[idx], engine.getVarByName(varList[idx]))
                        engine.addConstr(materialBalance, ConstraintType.eq, 0.0, "material_balance_${mat}_$idx")
                    }
                }
            }
        }
    }

    private fun createObjectives() {
        var obj = engine.linearNumExpr()
        for (t in 1..problem.period) {
            for ((mat, _) in problem.getProductOrders()) {
                val varName3 = "fulfill_${mat}"
                obj.addTerm(1.0, engine.getVarByName(varName3))
            }
        }
        engine.setObjective(obj, ObjectiveType.Maximize)
    }

    fun addUnits(resources: List<PlanUnit>) {
        problem.addUnits(resources)
    }

    /**
     * 添加工序可选设备
     */
    fun addRoutingUnits(rtUnits: List<StepUnitEntity>) {
        problem.addRoutingUnits(rtUnits)
    }

    /**
     * 添加周期
     */
    fun addPeriods(period: Int) {
        problem.addPeriods(period)
    }

    /**
     * 添加订单
     */
    fun addOrders(orders: List<OrderEntity>) {
        problem.addOrders(orders)
    }

    override fun createEngine() {
        this.engine = Algorithms.createDefaultSolver("supplychain")
    }

    override fun createProblem() {
        this.problem = SupplyChainProblem()
    }

    override fun initConfig() {
        this.config = SCPConfig()
    }

    override fun dispose() {
        problem.dispose()
        engine.dispose()
    }

    override fun export(path: String) {
        engine.export(path)
    }

    override fun exportData(path: String) {
        return
    }

    override fun loadData(path: String) {
        return
    }
}