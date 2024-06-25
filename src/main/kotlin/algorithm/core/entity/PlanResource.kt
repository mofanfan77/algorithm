package core.entity

import data.BaseData
import java.time.Duration
import java.time.LocalDateTime

class CalendarDatum(var start: LocalDateTime, var end: LocalDateTime) : BaseData() {
    var capacity = 0.0
    var comment = ""
    constructor(start: LocalDateTime, end: LocalDateTime, capacity: Double, comment: String): this(start, end){
        this.capacity = capacity
        this.comment = comment
    }
}

open class PlanResource : AlgoObject() {
    /**
     * 资源id
     */
    var resourceId = ""

    /**
     * 资源组id，用于处理多重资源约束
     */
    var groupId = 0

    /**
     * 最早可用时间
     */
    var startTime = 0.0

    /**
     * 不可用时间列表
     */
    var calendars = ArrayList<CalendarDatum>()

    @Deprecated("换用其他接口进行日历调用")
    fun addCalendar(start: LocalDateTime, end: LocalDateTime, zero: LocalDateTime) {
//        val data = CalendarDatum(Duration.between(zero, start).toSeconds().toDouble(), Duration.between(zero, end).toSeconds().toDouble())
//        calendars.add(data)
    }

    fun addCalendar(cal: PlanCalendar, zero: LocalDateTime) {
        val v1 = cal.startTime
        val v2 = cal.endTime
        val v3 = cal.capacity
        val v4 = cal.comment
        val cad = CalendarDatum(v1, v2, v3, v4)
        calendars.add(cad)
    }

    override fun toString(): String {
        return resourceId
    }
}