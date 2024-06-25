package catalog.solution

import core.entity.PlanEntity
import core.entity.PlanResource

class FJSPInitSolution {
    // 实体
    lateinit var entity : PlanEntity
    // 资源
    lateinit var resource: PlanResource

    fun isInitialized(): Boolean {
        return ::entity.isInitialized && ::resource.isInitialized
    }
}