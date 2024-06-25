package solver

import solver.datainterface.SnapShotInterface

abstract class BaseEngineSolver : SnapShotInterface {
    abstract fun createEngine()
    abstract fun createProblem()
    abstract fun initConfig()

    abstract fun dispose()

    /**
     * lp模型文件
     */
    abstract fun export(path:String)
}