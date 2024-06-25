package core.entity

import problem.BaseProblem

class VariableSequence {

    var taskSequence = ArrayList<Variable>()
    var problem: BaseProblem? = null
    private var calendarSequence = VariableQueue()
    private var finalSequence = VariableQueue()
    private var defaultTime = 0.0
    private var resource: PlanResource? = null
    private var timeAttributeMap: MutableMap<Attributes, Double> = mutableMapOf()

    constructor()
    constructor(problem: BaseProblem) {
        this.problem = problem
    }

    constructor(resource: PlanResource, problem: BaseProblem) {
        this.resource = resource
        this.problem = problem
        loadCalendar(resource)
    }

    /**
     * 加载资源上的可用日历
     */
    private fun loadCalendar(resource: PlanResource) {
        var toAdd = ArrayList<Variable>()
        val zero = this.problem!!.getTime()
        val unit = this.problem!!.getTimeUnit()
        for (cld in resource.calendars) {
            val block = Variable(cld, zero, unit)
            if (block.getStartTime() >= 0.0 && block.getEndTime() >= 0.0){
                toAdd.add(block)
            }
        }
        calendarSequence.addAll(toAdd.sortedBy { it.getStartTime() })
        finalSequence.addAll(toAdd.sortedBy { it.getStartTime() })
    }

    fun getId(): String {
        return this.resource?.resourceId ?: ""
    }

    /**
     * 最后一个任务结束时间
     */
    fun lastEndTime(): Double {
        return if (taskSequence.size == 0) {
            defaultTime
        } else {
            findLastJob()!!.getEndTime()
        }
    }

    fun getSize(): Int {
        return taskSequence.size
    }

    /**
     * 和前序变量连接
     */
    fun connect(variable: Variable) {
        val lastTask = taskSequence.lastOrNull()
        variable.previous = lastTask
        variable.owner = this
        lastTask?.let { it.next = variable }
        taskSequence.add(variable)
    }

    /**
     * 添加
     */
    fun add(variable: Variable) {
        calendarSequence.add(variable)
        variable.solveFlag = true
    }

    /**
     * 最后一个任务
     */
    fun findLastJob(): Variable? {
        return this.taskSequence.lastOrNull { it.block != null }
    }

    fun findNextAvailableTime(startTime: Double, duration: Double, update: Variable? = null): Double {
        val ans: Double = if (update != null) {
            if (update.solveFlag) {
                updateNextAvailableTime(startTime, duration)
            } else {
                initialNextAvailableTime(startTime, duration)
            }
        } else {
            initialNextAvailableTime(startTime, duration)
        }
        return ans
    }

    fun updateNextAvailableTime(startTime: Double, duration: Double): Double {
        var ans = startTime
        var lastSeq: Variable? = null
        if (finalSequence.size > 0) {
            for (seq in finalSequence) {// 先add新的Variable。循环时需要剔除
                if (seq.getEndTime() < startTime) {
                    continue
                }
                if (lastSeq == null) {
                    if (seq.getStartTime() - startTime >= duration) {
                        break
                    }
                } else {
                    val earliest = maxOf(startTime, lastSeq.getEndTime())
                    if (seq.getStartTime() - earliest >= duration) {
                        ans = earliest
                        break
                    }
                }
                lastSeq = seq
            }
        }
        return ans
    }

    /**
     * 开始时间之后的，长度为duration的下一个可用时间
     */
    fun initialNextAvailableTime(startTime: Double, duration: Double): Double {
        var ans = startTime
        var lastSeq: Variable? = null
        if (calendarSequence.size > 0) {
            for (seq in calendarSequence) {// 先add新的Variable。循环时需要剔除
                if (seq.getEndTime() < startTime) {
                    continue
                }
                if (lastSeq == null) {
                    if (seq.getStartTime() - startTime >= duration) {
                        break
                    }
                } else {
                    val earliest = maxOf(startTime, lastSeq.getEndTime())
                    if (seq.getStartTime() - earliest >= duration) {
                        ans = earliest
                        break
                    }
                }
                lastSeq = seq
            }
        }
        return ans
    }

    fun getNextCalendar(startTime: Double, endTime: Double = Double.MAX_VALUE): Variable? {
        var ans : Variable? = null
        for (seq in calendarSequence){
            if (seq.getEndTime() < startTime){ // 不在区间内
                continue
            }
            if (seq.getStartTime() > endTime){  // 不在区间内
                break
            }
            ans = seq
            break
        }
        return ans
    }

    fun getAvailableTime(startTime: Double, endTime:Double): Double{
        var ans = 0.0
        var lastSeq: Variable? = null
        for (seq in calendarSequence){
            if (seq.getEndTime() < startTime){ // 不在区间内
                continue
            }
            if (seq.getStartTime() > endTime){  // 不在区间内
                break
            }
            ans += maxOf(0.0, seq.getStartTime() - maxOf(startTime, lastSeq?.getEndTime()?:0.0))
            lastSeq = seq
        }
        ans += maxOf(0.0, endTime - (lastSeq?.getEndTime() ?: startTime))
        return ans
    }
    fun getAvailableEndTime(startTime: Double, duration:Double): Double{
        var ans = 0.0
        var cum = 0.0
        var lastSeq: Variable? = null
        var getLimit = false
        for (seq in calendarSequence){
            if (seq.getEndTime() < startTime){ // 不在区间内
                continue
            }
            if (cum + seq.getStartTime() -  maxOf(startTime, lastSeq?.getEndTime()?:0.0) > duration){// 加进来超过范围
                ans =  maxOf(startTime, lastSeq?.getEndTime()?:0.0) + (duration - cum)
                getLimit = true
                break
            }else{
                cum += maxOf(0.0, seq.getStartTime()-  maxOf(startTime, lastSeq?.getEndTime()?:0.0))
            }
            lastSeq = seq
        }
        if (!getLimit){
            ans = (duration - cum) + (lastSeq?.getEndTime() ?: startTime)
        }
        return ans
    }


    /**
     * 剔除设备日历的总时长
     */
    fun unavailableDuration(end: Double): Double {
        var value = 0.0
        for (seq in calendarSequence) {
            if (seq.getStartTime() <= end) {
                value += minOf(seq.getEndTime(), end) - maxOf(0.0, seq.getStartTime())
            } else {
                break
            }
        }
        return value
    }

    operator fun get(index: Int): Variable {
        return taskSequence[index]
    }

    /**
     * 寻找最早开始时间，使得结束时间在此之后
     */
    fun findPreviousStart(newStart: Double, prefixDuration: Double): Double {
        var startpoint = newStart - prefixDuration
        var endpoint = newStart
        if (calendarSequence.size > 0) {
            for (seq in calendarSequence) {
//                var seq = allSequence[idx]
                if (seq.block != null) { // 不是停机日历
                    continue
                }
                if (seq.getStartTime() > endpoint) {
                    break
                } // 超出停机时间
                if (seq.getEndTime() < startpoint) { // 没到停机开始
                    continue
                }
                // 出现重叠时间块
                startpoint = seq.getEndTime()
            }
        }
        return startpoint
    }

    /**
     * 对所有变量重新进行一次计算
     */
    fun resetForRecalculation() {
        timeAttributeMap.clear()
    }
}