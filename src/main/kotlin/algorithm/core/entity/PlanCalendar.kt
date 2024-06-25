package core.entity

import java.time.LocalDateTime

class PlanCalendar: AlgoObject() {
    var resourceId = ""
    var startTime = LocalDateTime.of(1970,1,1,0,0,0)
    var endTime = LocalDateTime.of(1970,1,1,0,0,0)
    var comment = ""
    var capacity = 0.0
}