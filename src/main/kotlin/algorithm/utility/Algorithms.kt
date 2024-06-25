package utility

import core.engine.BaseEngine
import core.engine.DefaultEngine
import core.engine.GDMIPEngine
import core.engine.ProEngine
import problem.BasicSchedulingProblem
import solver.BasicCAPSolver
import solver.BasicJSPSolver
import solver.BasicSCPSolver
import solver.PremiumCAPSolver
import utility.enums.EngineEnum
import utility.enums.SolverEnum

/**
 * <p> 工具类 </p>
 * Create by yang yx at  2024/05/20
 */
object Algorithms {

    fun createFJSPSolver(): BasicJSPSolver {
        val problem = BasicSchedulingProblem()
        problem.createNetwork()
        return BasicJSPSolver(problem)
    }

    fun createCAPSolver(): BasicCAPSolver {
        return BasicCAPSolver()
    }

    fun createCAPSolver(en: SolverEnum): BasicCAPSolver {
        return when( en){
            SolverEnum.PremCAP -> PremiumCAPSolver()
            SolverEnum.BasicCAP -> BasicCAPSolver()
        }
    }

    fun createSCPSolver(): BasicSCPSolver {
        return BasicSCPSolver()
    }

    fun createGDSolver(name: String): GDMIPEngine {
        return GDMIPEngine(name)
    }

    fun createDefaultSolver(name: String): DefaultEngine {
        return DefaultEngine(name)
    }

    fun createEngine(name:String, type : EngineEnum): BaseEngine {
        return when (type) {
            EngineEnum.Default -> GDMIPEngine(name)
            EngineEnum.ICPSolver -> DefaultEngine(name)
            EngineEnum.GBSolver -> ProEngine(name)
        }
    }

}