package core.entity

import solver.solverLog
import utility.annotation.Ignored
import utility.enums.AttrClassEnum
import utility.enums.AttrTypeEnum
import utility.enums.TimeUnit
import utility.resourcePrefix
import java.time.Duration
import java.time.LocalDateTime

class Variable{

    constructor()
    constructor(owner: VariableSequence, block: PlanEntity) {
        this.owner = owner
        this.block = block
    }

    constructor(block: PlanEntity, locate: PlanResource, eor: EntityOnResource) {
        this.block = block
        this.locate = locate
        this.relation = eor
    }
    /**
     * 生成设备日历专用构造方法
     * @param start 开始时间， end 结束时间
     */
    constructor( start: Double, end: Double) {
        this["start"] = start
        this["end"] = end
    }

    constructor( cal: CalendarDatum, timeZero: LocalDateTime, unit: TimeUnit) {
        val v1 = when (unit){
            TimeUnit.seconds -> Duration.between(timeZero, cal.start).toSeconds().toDouble()
            TimeUnit.minutes -> Duration.between(timeZero, cal.start).toMinutes().toDouble()
            TimeUnit.hours -> Duration.between(timeZero, cal.start).toHours().toDouble()
            TimeUnit.days -> Duration.between(timeZero, cal.start).toDays().toDouble()
        }

        val v2 = when (unit){
            TimeUnit.seconds -> Duration.between(timeZero, cal.end).toSeconds().toDouble()
            TimeUnit.minutes -> Duration.between(timeZero, cal.end).toMinutes().toDouble()
            TimeUnit.hours -> Duration.between(timeZero, cal.end).toHours().toDouble()
            TimeUnit.days -> Duration.between(timeZero, cal.end).toDays().toDouble()
        }

        this["start"] = v1
        this["end"] = v2
        this["capacity"] = cal.capacity
        this["comment"] = cal.comment
    }

    var block: PlanEntity? = null
    var locate: PlanResource? = null
    var secondaryLocates : ArrayList<PlanResource> = arrayListOf()
    var owner: VariableSequence? = null
    var previous: Variable? = null
    var next: Variable? = null
    var priorVariables = ArrayList<Variable>()
    var subsequenVariables = ArrayList<Variable>()
    var callbackTimes = 0
    var calcPriority = 0
    var solveFlag = false
    var relation: EntityOnResource? = null
    private var subSequence = mutableMapOf<String, SubVariableSequence>()
    private var prevSubSequence = mutableMapOf<String, Variable>()
    private var nextSubSequence = mutableMapOf<String, Variable>()
    private var attibutes = mutableMapOf<String, AttributeNode>()

    fun addToSub(key: SubVariableSequence, variable: Variable?){
        this.subSequence[key.id] = key
        variable?.let {prev ->
            this.prevSubSequence[key.id] = prev
            prev[key.id] = this
        }
    }

    /**
     * 多维资源连接
     */
    @Ignored
    fun addToSub(key: String, variable: Variable?){
        variable?.let {prev ->
            this.prevSubSequence[key] = prev
            prev[key] = this
        }
    }

    /**
     * 记录多维资源约束
     */
    @Ignored
    fun addToSub(secResource: PlanResource) {
        this.secondaryLocates.add(secResource)
    }

    fun create(attr: Attributes): AttributeNode?{
        var returnObject: AttributeNode? = null
        if (attr.name !in attibutes){
            returnObject = AttributeNode(attr, this)
            attibutes[attr.name] = returnObject
        }
        return returnObject
    }

    fun getStartTime(): Double {
        return (attibutes["start"]?.value as Double?) ?: 0.0
    }

    fun updateStartTime(time: Double){
        if (time > this.getStartTime()) {
            attibutes["earliest"]!!.update(time)
        }
    }

    fun getEarliestTime(): Double {
        return attibutes["earliest"]!!.value as Double
    }

    fun getEndTime(): Double {
        return (attibutes["end"]?.value as Double?) ?: 0.0
    }

    fun getProcessEndTime(): Double{
        return (attibutes["produceEnd"]?.value as Double?)?: 0.0
    }

    fun getPrefixEndTime(): Double{
        return (attibutes["prefixEndTime"]?.value as Double?) ?: 0.0
    }

    fun getPrefixStartTime(): Double{
        return (attibutes["prefixStartTime"]?.value as Double?) ?: 0.0
    }

    fun getEndTimeOnSameMachine(): Double {
        val flag = (attibutes["postDurationFlag"]?.value ?: false) as Boolean
        return if (flag) {
            getProcessEndTime()
        }else{
            getEndTime()
        }
    }

    fun getProcessDuration(): Double {
        return this.getProcessEndTime() - this.getStartTime()
    }

    fun getPrefixDuration(): Double {
        return this.getPrefixEndTime() - this.getPrefixStartTime()
    }

    fun getViceResource(): MutableMap<String, Double>{
        return this.relation!!.subResourceUtil
    }

    fun getViceResourceQuantity(viceId: String): Double{
        return this.relation!!.subResourceUtil[viceId]!!
    }


    fun addAsPrior(priors: List<Variable>) {
        this.priorVariables.addAll(priors)
    }

    fun addAsSubsequent(priors: List<Variable>) {
        this.subsequenVariables.addAll(priors)
    }

    fun getId(): String{
        return this.block?.uniqueId ?:""
    }

    @Ignored
    fun getTimeZero(): LocalDateTime{
        return owner?.problem?.getTime() ?: LocalDateTime.now()
    }

    @Ignored
    fun getTimeUnit():TimeUnit{
        return owner?.problem?.getTimeUnit() ?: TimeUnit.seconds
    }

    fun getPreviousFromGroup(name:String): Variable?{
        return this.prevSubSequence[name]
    }

    fun getPreviousFromResource(group: Int): Variable?{
        val name = "$resourcePrefix$group"
        return this.prevSubSequence[name]
    }

    operator fun get(index: String): AttributeNode {
        require(index in attibutes) { "此属性 $index 未提前定义" }
        return attibutes[index]!!
    }

    operator fun get(index: Attributes): AttributeNode {
        require(index.name in attibutes) { "此属性 $index 未提前定义" }
        return attibutes[index.name]!!
    }

    operator fun set(varName: Attributes, value: Any) {
        try {
            set(varName.name, value)
        }catch (e: Exception){
            solverLog.error(e.message)
        }
    }

    operator fun set(varName: String, value: Any) {
        if (varName !in attibutes) {
            val attr = when(value) {
                is Boolean -> BooleanAttribute(varName, varName, AttrTypeEnum.Decision, AttrClassEnum.BinVar)
                is Double -> DoubleAttribute(varName, varName, AttrTypeEnum.Decision, AttrClassEnum.DoubleVar)
                is String -> StrAttribute(varName, varName, AttrTypeEnum.Decision, AttrClassEnum.StrVar)
                is Variable -> EntityAttribute(varName, varName, AttrTypeEnum.Decision, AttrClassEnum.EntVar)
                else -> {throw Exception("设置属性值，使用了不支持的数据类型 $value::class") }
            }
            attibutes[varName] = AttributeNode(attr, this)
        }
        attibutes[varName]!!.value = value
    }


    override fun toString(): String {
        return (this.block?.uniqueId ?: "") + "-" + this.getStartTime() + "-" + this.getEndTime()
    }
}
