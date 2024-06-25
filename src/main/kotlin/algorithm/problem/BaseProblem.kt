package problem

import analysis.Objective
import analysis.ObjectiveBuilder
import core.constraint.*
import core.entity.*
import utility.enums.AttrClassEnum
import utility.enums.AttrTypeEnum
import utility.enums.ObjectiveEnum
import utility.exception.VariableGroupDefinationException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

abstract class BaseProblem : AbstractProblem() {
    var builder = ObjectiveBuilder()
    var generator = ConstraintGenerator()

    private var parameterSet: MutableMap<String, Pair<Any, Any>> = mutableMapOf()
    abstract fun initialize()

    override fun statistics() {
        generator.printStaistics()
    }

    fun updateStatistics(){
        generator.updateStatistics()
    }


    //    =================================================
    /**
     * 新增约束和变量
     */

    fun addIfConstraint(name: String, result: Attributes, vararg sourceAttr: Attributes) {
        val thresh = sourceAttr[0]
        val condition = sourceAttr[1]
        val trueVal = sourceAttr[2]
        val falseVal = sourceAttr[3]
        val constr = ConditionConstraint(name, result, thresh, condition, trueVal, falseVal)
        this.generator.addConstraint(constr, result.name)
    }

    fun addObjective(
        name: String,
        formula: (List<Variable>) -> Double,
        weight: Double,
        type: ObjectiveEnum,
        level: Int = 1
    ) {
        this.builder.addObjective(name, formula, weight, type, level)
    }

    fun addCumtomConstraint(name: String, func: (Variable, Attributes, List<Attributes>) -> Unit, target: Attributes, vararg param: Attributes) {
        val sources = ArrayList<Attributes>()
        sources.addAll(param.toList())
        val constr = CustomConstraint(name, func, target, sources)
        this.generator.addConstraint(constr, name)
    }

    /**
     * "1.2.2 声明是否参与重优化, 默认false"
     */
    fun addCumtomConstraint(name: String, func: (Variable, Attributes, List<Attributes>) -> Unit, target: Attributes, reOpt: Boolean, vararg param: Attributes) {
        val sources = ArrayList<Attributes>()
        sources.addAll(param.toList())
        val constr = CustomConstraint(name, func, target, sources)
        this.generator.addConstraint(constr, name, reOpt)
    }

    /**
     * 分组约束 必须是重优化约束
     */
    fun addGroupCustomConstraint(groupName: String, name: String, func: (Variable, Attributes, List<Attributes>, String) -> Unit, target: Attributes, vararg param: Attributes) {
        val sources = ArrayList<Attributes>()
        sources.addAll(param.toList())
        val constr = GroupCustomConstraint(groupName, name, func, target, sources)
        this.generator.addConstraint(constr, name, true)
    }

    fun addDependencyCumtomConstraint(name: String,
                                      func: (Variable, List<Variable>, Attributes, List<Attributes>) -> Unit,
                                      dependency: (Variable) -> List<Variable>,
                                      target: Attributes, reOpt: Boolean = false, vararg param: Attributes) {
        val sources = ArrayList<Attributes>()
        sources.addAll(param.toList())
        val constr = CustomDepencyConstraint(name, func, dependency, target, sources)
        this.generator.addConstraint(constr, name, reOpt)
    }


    // 添加日历约束
    // 可能需要迁移到FJSP
    fun addCalendarConstraint(name: String, param1: Attributes, startAttr: Attributes, durationAttr: Attributes) {
        val constr = CalendarConstraint(name, param1, startAttr, durationAttr)
        this.generator.addConstraint(constr, param1.name)
    }

    // 添加前序属性
    fun addPreviousConstraint(name: String, param1: Attributes, prevAttr: Attributes) {
        val constr = PreviousConstraint(name, param1, prevAttr)
        this.generator.addConstraint(constr, param1.name)
    }

    /**
     * target表示目标变量, source为后序变量的属性
     */
    fun addNextConstraint(name: String, target: Attributes, source: Attributes) {
        val constr = SubseqConstraint(name, target, source)
        this.generator.addConstraint(constr, target.name)
    }

    fun addSumConstraint(name: String, result: Attributes, attrList: Array<out Attributes>) {
        val constr = SumConstraint(name, result, attrList)
        this.generator.addConstraint(constr, result.name)
    }

    fun addMinusConstraint(name: String, result: Attributes, attrList: Array<out Attributes>) {
        val constr = MinusConstraint(name, result, attrList)
        this.generator.addConstraint(constr, result.name)
    }

    fun addMaximizeConstraint(name: String, result: Attributes, attrList: Array<out Attributes>) {
        val constr = MaximizeConstraint(name, result, attrList)
        this.generator.addConstraint(constr, result.name)
    }

    @Deprecated("不再开发专用的参数接口")
    fun addParameter(maxRule: String, maxCleanParam: Any, maxCleanDuration: Any?) {
        if (maxCleanDuration == null) {
            this.parameterSet[maxRule] = Pair(maxCleanParam, maxCleanParam)
        } else {
            this.parameterSet[maxRule] = Pair(maxCleanParam, maxCleanDuration)
        }
    }

    fun getParameter(enumName: String): Pair<Any, Any>? {
        return this.parameterSet[enumName]
    }

    fun addVariable(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) {
        val variable: Attributes = when (group) {
            AttrClassEnum.BinVar -> BooleanAttribute(name, desc, type, group)
            AttrClassEnum.DoubleVar -> DoubleAttribute(name, desc, type, group)
            AttrClassEnum.StrVar -> StrAttribute(name, desc, type, group)
            AttrClassEnum.EntVar -> EntityAttribute(name, desc, type, group)
        }
        this.generator.addVariable(variable)
    }

    fun addVariableGroup(name: String, vararg attributes: String) {
        var targets = this.generator.mapAttributes(*attributes)
        if (targets.any { it.type != AttrTypeEnum.Constant }) {
            throw VariableGroupDefinationException("变量组定义需为parameter类型，以下可变值不可作为groupId, ${
                targets.filter { it.type != AttrTypeEnum.Constant }.map { it.name }.joinToString("; ")
            }")
        }
        this.generator.addGroup(name, targets)
    }

    fun getObjective(): Objective {
        return this.builder.build()
    }

    fun setObjectiveTolerance(level: Int, relativeTolerance: Double) {
        this.builder.setObjectiveTolerance(level, relativeTolerance)
    }
}