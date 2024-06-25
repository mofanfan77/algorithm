package solver

import catalog.component.SolverManager
import catalog.ea.ChromoseBase
import catalog.solution.FJSPInitSolution
import config.JSPConfig
import core.algorithm.AlgorithmFactory
import core.entity.*
import data.snapshot.FJSPSnapShot
import data.snapshot.ModelSnapshot
import problem.BasicSchedulingProblem
import utility.enums.Constr
import utility.enums.ParameterEnum
import utility.enums.TimeUnit
import utility.exception.InitializationException
import java.time.LocalDateTime

class BasicJSPSolver(var problem: BasicSchedulingProblem) : BaseSolver(problem) {
    /**
     * problem: 参数存储
     */
    private var manager = SolverManager(problem)
    // 算法
    init {
        algorithmParameter = JSPConfig()
    }
    // 求解器管理器

    override fun loadAlgorithm() {
        algorithm = AlgorithmFactory.createHybridAlgorithm(this)
    }


    override fun validate(){
        val v1 = algorithmParameter.getAsInt(ParameterEnum.NumberResource)
        val v2 = algorithmParameter.getAsBoolean(ParameterEnum.MultiResource)
        if (problem.resourceLevels != v1){
            throw InitializationException("传入的资源级数为${problem.resourceLevels}, 设置的参数为${v1}")
        }
        if (v2 && v1 == 1){ // 退化成单资源
            algorithmParameter.set(ParameterEnum.NumberResource, 1)
            solverLog.info("修改多重资源数量，传入的资源级数为1")
        }
        if (!v2 && v1 > 1){
            throw InitializationException("未设置多重资源数量，传入的资源级数为${problem.resourceLevels} > 1")
        }
    }

    /**
     * 求解方法
     */
    override fun solve() {
        try {
            this.validate()
            this.initialize()
            this.execute()
            this.endSearch()
        } catch (e: Exception) {
            solverLog.error(e.stackTraceToString())
        }
    }

    override fun exportData(path: String) {
        this.initialize()
        val exportion = FJSPSnapShot()
        exportion.objective = this.problem.builder
        exportion.inputs = this.problem.collectInputs()
        exportion.constraints = this.problem.collectConstraints()
        exportion.variables = this.problem.generator.getAttributes()
        exportion.config = this.algorithmParameter
        ModelSnapshot.serialize(exportion, path)
    }

    @Deprecated("替换成 exportData")
    fun exportModel(path: String) {
        this.initialize()
        val exportion = FJSPSnapShot()
        exportion.objective = this.problem.builder
        exportion.inputs = this.problem.collectInputs()
        exportion.constraints = this.problem.collectConstraints()
        exportion.variables = this.problem.generator.getAttributes()
        exportion.config = this.algorithmParameter
        ModelSnapshot.serialize(exportion, path)
    }

    override fun loadData(path: String) {
        val model = ModelSnapshot.deserialize(path) as FJSPSnapShot
        this.algorithmParameter = model.config
        this.problem.recover(model)
    }

    @Deprecated("替换成 loadData")
    fun loadModel(path: String) {
        val model = ModelSnapshot.deserialize(path) as FJSPSnapShot
        this.algorithmParameter = model.config
        this.problem.recover(model)
    }

    /**
     *  添加工件方法
     */
    fun addEntities(jobs: List<PlanEntity>) {
        solverLog.info("共添加${jobs.size}个工件")
        problem.addEntities(jobs)
    }

    /**
     * 添加机器方法
     */
    fun addMachines(machines: List<PlanResource>) {
        solverLog.info("共添加${machines.size}个主资源")
        problem.addResources(machines)
    }

    /**
     * 添加副资源
     */
    fun addVices(vices: List<PlanSubResource>) {
        problem.addViceResource(vices)
    }

    /**
     * 添加加工时间方法
     */
    fun addProcessTimes(pieceStepOnResources: Collection<EntityOnResource>) {
        problem.addEntityOnResources(pieceStepOnResources)
    }

    /**
     * 添加机器不可用时间方法
     */
    fun addUnavailableTimes(calendars: Collection<PlanCalendar>) {
        problem.addCalendars(calendars)
    }

    fun setSystemTime(time: LocalDateTime) {
        problem.setTime(time)
    }

    fun setTimeUnit(unit: TimeUnit){
        problem.setTimeUnit(unit)
    }

    /**
     * 添加自定义约束
     */
    fun addConstraint(
        func: (Variable, Attributes, List<Attributes>) -> Unit,
        target: Attributes,
        vararg param: Attributes
    ) {
        val name = target.name
        problem.addCumtomConstraint(name, func, target, *param)
    }

    fun addConstraint(
        func: (Variable, Attributes, List<Attributes>) -> Unit,
        targetAttr: String,
        vararg sourceAttr: String
    ) {
        val target = problem.generator.mapAttribute(targetAttr)
        val sources = problem.generator.mapAttributes(*sourceAttr)
        problem.addCumtomConstraint(targetAttr, func, target, *sources)
    }

    fun addConstraint(
        func: (Variable, Attributes, List<Attributes>) -> Unit,
        targetAttr: String,
        reopt: Boolean,
        vararg sourceAttr: String
    ) {
        val target = problem.generator.mapAttribute(targetAttr)
        val sources = problem.generator.mapAttributes(*sourceAttr)
        problem.addCumtomConstraint(targetAttr, func, target, reopt, *sources)
    }

    fun addConstraintByGroup(
        groupName: String,
        func: (Variable, Attributes, List<Attributes>, String) -> Unit,
        targetAttr: String,
        vararg sourceAttr: String
    ){
        val target = problem.generator.mapAttribute(targetAttr)
        val sources = problem.generator.mapAttributes(*sourceAttr)
        problem.addGroupCustomConstraint(groupName, targetAttr, func, target, *sources)
    }

    fun addConstraintWithDependency(
        func: (Variable, List<Variable>, Attributes, List<Attributes>) -> Unit,
        dependency: (Variable) -> List<Variable>,
        targetAttr: String,
        vararg sourceAttr: String
    ) {
        val target = problem.generator.mapAttribute(targetAttr)
        val sources = problem.generator.mapAttributes(*sourceAttr)
        problem.addDependencyCumtomConstraint(targetAttr, func, dependency, target, reOpt = false, *sources)
    }

    /**
     * 添加指定约束
     */
    fun addConstraint(type: Constr, targetAttr: Attributes, vararg sourceAttr: Attributes) {
        val name = targetAttr.name
        try {
            when (type) {
                Constr.Precedent -> {
                    if (sourceAttr.size != 1) {
                        throw Exception("${name}_输入参数数量有误")
                    }
                    problem.addPriorConstraint(name, targetAttr, sourceAttr[0])
                }

                Constr.Calendar -> {
                    if (sourceAttr.size != 2) {
                        throw Exception("${name}_输入参数数量有误")
                    }
                    problem.addCalendarConstraint(name, targetAttr, sourceAttr[0], sourceAttr[1])
                }

                Constr.Previous -> {
                    if (sourceAttr.size != 1) {
                        throw Exception("${name}__输入参数数量有误")
                    }
                    problem.addPreviousConstraint(name, targetAttr, sourceAttr[0])
                }
                Constr.Maximize -> {
                    if (sourceAttr.isEmpty()) {
                        throw Exception("${name}__输入参数为空")
                    }
                    problem.addMaximizeConstraint(name, targetAttr, sourceAttr)
                }
                Constr.Sum -> {
                    if (sourceAttr.isEmpty()) {
                        throw Exception("${name}__输入参数为空")
                    }
                    problem.addSumConstraint(name, targetAttr, sourceAttr)
                }
                Constr.Condition -> {
                    if (sourceAttr.size != 4) {
                        throw Exception("${name}__输入参数必须为4")
                    }
                    problem.addIfConstraint(name, targetAttr, *sourceAttr)
                }
                Constr.CleanRule -> {
                    if (sourceAttr.size != 3) {
                        throw Exception("${name}__输入参数必须为3")
                    }
                    problem.addCleanRuleConstraint(name, targetAttr, sourceAttr[0], sourceAttr[1], sourceAttr[2])
                }
                Constr.Subsequent -> {
                    problem.addNextConstraint(name, targetAttr, sourceAttr[0])
                }
                Constr.Minus -> {
                    if (sourceAttr.isEmpty()) {
                        throw Exception("${name}__输入参数为空")
                    }
                    problem.addMinusConstraint(name, targetAttr, sourceAttr)
                }
            }
        } catch (e: Exception) {
            solverLog.error(e.message)
        }

    }

    /**
     * 提供
     */
    fun addConstraint(type: Constr, targetAttr: String, vararg sourceAttr: String) {
        val target = problem.generator.mapAttribute(targetAttr)
        val sources = problem.generator.mapAttributes(*sourceAttr)
        this.addConstraint(type, target, *sources)
    }

    /**
     * 传入初始解
     */
    fun initSolution(initSolution: List<FJSPInitSolution>) {
        problem.readInitialSolution(initSolution)
    }

    // 获取信息接口
    fun getInfoOfJobs() {}
    fun getInfoOfMachines() {}
    fun getInfoOfObjectives() {}
    fun getStatus() {}

    @Deprecated("目标函数已废弃")
            /**
             * 设置求解时间（秒）
             */
    fun setSolveTime(seconds: Int) {
//        algorithm.parameter.solveLimit = seconds.toLong()
    }


    fun evaluate(chromosome: ChromoseBase) {
        this.algorithm.evaluate(chromosome)
        val result = this.manager.parseSolution(chromosome.solution)
        this.updateSolution(result)
    }

    /**
     * 版本 1 适配规则
     * @param links: 三元组，前置eor, 后置eor, 是否禁用Boolean
     */
    fun addLinks(links: List<Triple<EntityOnResource, EntityOnResource, Boolean>>) {
        problem.rules.addLinks(links)
    }

    private fun execute() {
        solverLog.info("algorithm start initialize ... with ${Runtime.getRuntime().availableProcessors()} cores")
        algorithm.start()
        solverLog.info("algorithm start running ...")
        algorithm.search()
        solverLog.info("algorithm end searching ...")
        algorithm.end()
    }

    private fun endSearch() {
        this.problem.statistics()
        val bestSolution = algorithm.getResult()
        val original = manager.parseSolution(bestSolution)
        this.updateSolution(original)
    }

    fun getBestResult(): List<Variable>{
        val bestSolution = algorithm.getResult()
        return bestSolution?.getResult() ?: arrayListOf()
    }

    fun debugRun(){
        this.initialize()
        algorithm.initialEvaluate()
    }


}

