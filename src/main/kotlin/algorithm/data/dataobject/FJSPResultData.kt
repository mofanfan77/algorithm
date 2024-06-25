package data.dataobject

import data.BaseData

class FJSPResultData(var machine:String, var operation:String,
                     var prefixStart: Double, var prefixEnd: Double,
                     var startTime: Double, var endTime: Double): BaseData() {

    override fun toString(): String {
        return "$machine,$operation,$startTime,$endTime"
    }
}