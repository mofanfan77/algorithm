package core.algorithm

import analysis.evalutor.JSPEvaluator
import analysis.evalutor.JSPFlexiblePathEvaluator
import analysis.evalutor.JSPMultiLayerEvaluator
import analysis.evalutor.RoutingEvaluator
import solver.BasicJSPSolver
import solver.CAPSolver
import utility.annotation.Ignored
import utility.enums.ParameterEnum

class AlgorithmFactory {
    companion object {
        @Ignored
        fun createHybridAlgorithm(solver: BasicJSPSolver): AlgorithmInterface {
            var obj: JobShopHybridAlgorithm
            if (solver.algorithmParameter.getAsBoolean(ParameterEnum.MultiResource)) { // 多重资源约束
                val layer = solver.algorithmParameter.getAsInt(ParameterEnum.NumberResource)
                val multiPath = solver.algorithmParameter.getAsBoolean(ParameterEnum.FlexiblePath)
                obj = MultiLayerHybridAlgorithm(layer, multiPath)
                obj.evaluator = JSPMultiLayerEvaluator(solver.problem)
            }else if(!solver.algorithmParameter.getAsBoolean(ParameterEnum.FlexiblePath)) { // 单路径
                obj = SinglePathHybridAlgorithm()
                obj.evaluator = JSPEvaluator(solver.problem)
            } else { // 多路径
                obj = FlexiblePathHybridAlgorithm()
                obj.evaluator = JSPFlexiblePathEvaluator(solver.problem)
            }
            obj.problem = solver.problem
            obj.parameter = solver.algorithmParameter
            obj.evaluator.getParameter(obj.parameter)
            obj.register()
            return obj
        }

        @Ignored
        fun createSimpleHybridAlgorithm(solver: CAPSolver): AlgorithmInterface {
            var obj = SimpleHybridAlgorithm()
            obj.evaluator = RoutingEvaluator()
            obj.problem = solver.problem
            obj.parameter = solver.config
            return obj
        }
    }
}
