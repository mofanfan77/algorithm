package analysis
import core.entity.Variable
import solver.solverLog
import utility.exception.ObjectiveComparionError

open class Objective : Comparable<Objective> {
    var metrics = mutableMapOf<Int, ArrayList<Metric>>()
    var fitness : ArrayList<Double> = arrayListOf()
    lateinit var builder: ObjectiveBuilder

    fun initial(builder: ObjectiveBuilder) {
        this.builder = builder
        for((level,v) in builder.metrics) {
            fitness.add(0.0)
            this.metrics[level] = arrayListOf()
            for (m in v) {
                this.metrics[level]!!.add(m.create())
            }
        }
    }

    /**
     * @param 在某个阈值 realSolution 下， 是否比 ${fitness} 更好
     */
    fun outperform(fitness: Objective, realSolution: Array<Double>) : Boolean {
        val a = this.fitness
        val b = fitness.fitness
        val thresh = Array<Double>(a.size){0.0}
        val tol = fitness.builder.tolerance.values.toList()
        for (i in realSolution.indices){
            if (realSolution[i] < 0){
                thresh[i] = realSolution[i] * (1+tol[i])
            }else{
                thresh[i] = realSolution[i] * (1-tol[i])
            }
        }
        var outperform = true
        for (i in (0 until  (a.size - 1))){
            if (b[i] < thresh[i]){
                break
            }
            if (a[i] < thresh[i]){
                outperform = false
                break
            }
        }
        for (j in (0 until a.size)){
            val i = a.size - 1 - j
            if (b[i] > a[i]){
                outperform = false
                break
            }else if (b[i] < a[i]){
                outperform = true
                break
            }
        }
        return outperform
    }

    override fun compareTo(other: Objective): Int {
        var result = 0
        if (this.fitness.size != other.fitness.size){
            throw ObjectiveComparionError("${this} 和 ${other}有不同的目标层级，无法比较")
        }
        for (idx in this.fitness.indices){
            val curV = this.fitness[idx]
            val tarV = other.fitness[idx]
            result = curV.compareTo(tarV)
            if (result != 0) break
        }
        return result
    }

    override fun toString(): String {
        var res = ""
        for ((level, entrys) in metrics){
            res += "===== Level:$level ===== \n"
            for (entry in entrys) {
                val k = entry.name
                val v = entry.value
                res += "name: $k == value: $v == weight: ${entry.weight} ==\n"
            }
        }
        return res
    }

    fun log(){
        solverLog.info(" 适应度为 ${this.fitness}")
        for ((level, entrys) in this.metrics){
            solverLog.info("【level】at level$level")
            for (entry in entrys) {
                val k = entry.name
                val v = entry.value
                solverLog.info("【Objective name】${k}, 【Objective value】${v}, 【Objective weight】${entry.weight}")
            }
        }
    }

    fun calcObjective(result: List<Variable>) {
        for ((level, entres) in this.metrics){
            var total = 0.0
            for (entry in entres) {
                entry.run(result)
                total += entry.getFitness()
            }
            this.fitness[level-1] = total
        }
    }

    operator fun minus(other: Objective): Double{
        var total = 0.0
        if (this.fitness.size != other.fitness.size){
            throw ObjectiveComparionError("${this} 和 ${other}有不同的目标层级，无法计算")
        }
        for (idx in this.fitness.indices){
            val curV = this.fitness[idx]
            val tarV = other.fitness[idx]
            total = curV - tarV
            if (total != 0.0) break
        }
        return total
    }
}
