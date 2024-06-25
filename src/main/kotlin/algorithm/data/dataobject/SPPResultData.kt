package data.dataobject

import data.BaseData

class SPPResultData(var operation:String, var index: Int): BaseData() {
    override fun toString(): String {
        return "$index,$operation"
    }
}