package problem

import core.entity.*
import data.snapshot.CAPSnapShot
import java.io.Serializable
import kotlin.math.abs

class CraneAssignProblem: AbstractProblem() {
    private var resource = mutableListOf<MoveResource>()
    private var nodes = mutableListOf<NodeEntity>()
    private var tasks = mutableListOf<MoveEntity>()
    private var assignment = mutableMapOf<MoveResource, List<MoveEntity>>()
    private var relationPools = mutableMapOf<MoveEntity, List<MoveResource>>()
    private var _repairList = mutableListOf<NodeEntity>()
    private var _fallingList = mutableListOf<NodeEntity>()
    private var taskDistanceMatrix = ArrayList<ArrayList<Double>>()

    var repairList: MutableList<NodeEntity>
        get() = _repairList
        set(value) {
            _repairList = value
        }

    var fallingList: MutableList<NodeEntity>
        get() = _fallingList
        set(value) {
            _fallingList = value
        }

    fun readAssignment(map: MutableMap<MoveResource, List<MoveEntity>>){
        var entities = HashSet<MoveEntity>()
        for ((k, v) in map.entries){
            assignment[k] = v
            entities.addAll(v)
        }
        this.addResource(map.keys.toList())
        this.addTasks(entities.toMutableList())
    }

    fun getPools(): Map<MoveEntity, List<MoveResource>>{
        return relationPools
    }

    fun addResource(res: List<MoveResource>){
        var idx = 1
        for (tas in res){
            tas.algoNr = idx
            idx ++
        }
        resource.addAll(res)
    }

    fun addNode(nodes: List<NodeEntity>){
        var idx = 1
        for (tas in nodes){
            tas.algoNr = idx
            idx ++
        }
        this.nodes.addAll(nodes)
    }
    fun getNode(): MutableList<NodeEntity>{
        return nodes
    }

    fun getResource(): MutableList<MoveResource>{
        return resource
    }

    fun addTasks(task: List<MoveEntity>){
        var idx = 1
        for (tas in task){
            tas.algoNr = idx
            idx ++
        }
        tasks.addAll(task)
    }

    fun getTask(): MutableList<MoveEntity>{
        return tasks
    }

    /**
     * 恢复数据
     */
    fun recover(model: CAPSnapShot) {
        this.tasks = (model.data[1] as List<MoveEntity>).toMutableList()
        this.nodes = (model.data[2] as List<NodeEntity>).toMutableList()
        this.resource = (model.data[3] as List<MoveResource>).toMutableList()
    }

    /**
     * 存入数据
     */
    fun collect(): MutableMap<Int, List<Serializable>> {
        val res = mutableMapOf<Int, List<Serializable>>()
        res[1] = this.tasks
        res[2] = this.nodes
        res[3] = this.resource
        return res
    }

    fun calcDistance(){
        for (i in 1..this.getTask().size) {
            taskDistanceMatrix.add(arrayListOf())
            for (j in 1..this.getTask().size) {
                val t1 = this.getTask()[i - 1]
                val t2 = this.getTask()[j - 1]
                val dist = abs(t2.origin.x - t1.dest.x)
                taskDistanceMatrix[i - 1].add(dist)
            }
        }
    }

    /**
     * 传入过滤列表后，查找最近的点
     */
    fun findClosest(last: Int, visited: HashSet<Int>) : Int {
        val look = taskDistanceMatrix[last - 1]
        var best = -1
        var bestValue = Double.MAX_VALUE
        for (j in 1..this.getTask().size){
            if (j !in visited){
                if (bestValue > look[j-1]){
                    bestValue = look[j-1]
                    best = j
                }
            }
        }
        return best
    }

    override fun dispose() {
        resource.clear()
        nodes.clear()
        tasks.clear()
        assignment.clear()
        relationPools.clear()
        _repairList.clear()
        _fallingList.clear()
        taskDistanceMatrix.clear()
    }
}