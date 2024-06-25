package catalog.solution

import data.dataobject.CAPResultData
import data.dataobject.CAPTraceData

class CAPSolution {
    var trace = ArrayList<CAPTraceData>()
    var assignment = ArrayList<CAPResultData>()

    fun addTrace(obj: CAPTraceData){
        trace.add(obj)
    }

    fun addAssignment(obj: CAPResultData){
        assignment.add(obj)
    }

    fun dispose(){
        trace.clear()
        assignment.clear()
    }
}