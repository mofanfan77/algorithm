package data

class DataGroup {
    /**
     * 规划资源
     */
    var resource: List<String> = arrayListOf()
    /**
     * 规划副资源
     */
    var vices: List<String> = arrayListOf()
    /**
     * 规划任务
     */
    var piecestep: List<String> = arrayListOf()
    /**
     * 规划资源可用日历
     */
    var calendar: List<String> = arrayListOf()
    /**
     * 规划任务资源信息
     */
    var pieceStepOnResource: List<String> = arrayListOf()
}