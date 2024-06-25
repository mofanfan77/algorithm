package core.entity

class VariableQueue {
    // 用开始时间排序
    private var queue = mutableListOf<Variable>()
    var size = 0.0
    fun add(ele: Variable){
        val index = queue.binarySearch(ele, Comparator.comparing(Variable::getStartTime))
        if (index < 0){
            queue.add(-index - 1, ele)
        }else{
            queue.add(index, ele)
        }
        size += 1
    }

    fun addAll(ele: List<Variable>){
        for (e in ele){
            this.add(e)
        }
    }

    fun clear(){
        queue.clear()
    }

    operator fun iterator(): Iterator<Variable>{
        return queue.iterator()
    }
}