package problem

import utility.enums.TimeUnit
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

abstract class AbstractProblem {
    private var systemTime: LocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)
    private var unit: TimeUnit = TimeUnit.seconds
    fun getTime(): LocalDateTime {
        return this.systemTime
    }

    //
    fun setTime(time: LocalDateTime) {
        this.systemTime = time
    }

    /**
     * @param time: "seconds", "minutes", "hours", "day"
     */
    fun setTimeUnit(time: TimeUnit) {
        this.unit = time
    }

    fun getTimeUnit(): TimeUnit {
        return this.unit
    }

    /**
     * 清理内存
     */
    abstract fun dispose()

    /**
     * statistics
     */
    open fun statistics(){}
}