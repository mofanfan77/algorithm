package analysis

import core.entity.Attributes
import core.entity.Variable
import core.entity.VariableSequence

class FJSPObjective : Objective() {
    /**
     * 通过伴生方法定义不同的目标函数
     */
    companion object {
        val totalTime: (List<Variable>) -> Double = { list ->
            var ans = 0.0
            for (seq in list) {
                if (ans <= seq.getEndTime()) {
                    ans = seq.getEndTime()
                }
            }
            ans / 3600.0
        }
        val finishRatio: (List<Variable>) -> Double = { list ->
            var ans = 0.0
            var count = 0.0
            for (seq in list) {
                if (Attributes.equalsTo(seq["lastStepFlag"].getAsBoolean(), true)) {
                    count += seq["quantity"].value as Double
                    if (Attributes.greater(seq["dueTime"].getAsDouble(), seq["end"].getAsDouble())) {
                        ans += seq["quantity"].value as Double
                    }
                }
            }
            if (count > 0) ans / count
            else 0.0
        }
        val switchTime: (List<Variable>) -> Double = { list ->
            var ans = 0.0
            for (seq in list) {
                ans += seq.getPrefixDuration()
            }
            ans / 3600.0
        }
        val utilization: (List<Variable>) -> Double = { list ->
            var num = 0.0
            var denom = 0.0
            var maxDuration = mutableMapOf<String, Double>()
            var workDuration = mutableMapOf<String, Double>()
            var sequenceList = HashSet<VariableSequence>()
            for (seq in list) {
                seq.owner?.let {
                    val key = it.getId()
                    maxDuration.putIfAbsent(key , 0.0)
                    workDuration.putIfAbsent(key, 0.0)
                    maxDuration[key] = maxOf(maxDuration[key]?:0.0, seq.getEndTimeOnSameMachine())
                    workDuration[key] = workDuration[key]!! + seq.getProcessDuration()
                    sequenceList.add(it)
                }
            }
            for ( u in maxDuration.values){ denom += u}
            for ( u in workDuration.values){ num += u}
            for (it in sequenceList){
                denom -= it.unavailableDuration(maxDuration[it.getId()]!!)
            }
            num / denom
        }
    }
}