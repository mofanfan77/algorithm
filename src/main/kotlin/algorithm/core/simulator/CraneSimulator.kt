package core.simulator

import core.entity.Coordinate
import core.entity.EventEntity

class CraneSimulator {
    private var speed = 0.0
    private var timeHorizon = 0
    private var tick = 0
    private var scheduler = Scheduler()

    fun setSpeed(speed: Double){
        this.speed = speed
    }

    fun setTimeHorizon(horizon:Int){
        this.timeHorizon = horizon
    }

    fun reset(){ tick = 0 }
    fun clock(){ tick += 1}
    fun checkStop(): Boolean{
        return tick >= timeHorizon
    }
    /**
     * 仿真核心逻辑
     * @param 输入一系列指令，包含移动、任务
     * @return 输出时间轨迹, key为运动物体 value为时间轨迹
     */
    fun run(commands: MutableMap<Coordinate, ArrayList<Coordinate>>): MutableMap<Coordinate, ArrayList<EventEntity>>{
        scheduler.initialize(commands)
        while (checkStop()){
            this.clock()
            scheduler.run()
        }
        this.reset()
        var tasks = scheduler.collect()
        // generate tasks based on info
        return tasks
    }
}