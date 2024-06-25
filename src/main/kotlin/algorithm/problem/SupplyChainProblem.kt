package problem

import core.entity.supplychain.*
import utility.Util
import utility.exception.InputDataNotFoundException

class SupplyChainProblem : AbstractProblem(){
    //    private var paths : MutableMap<String, ProductRouteEntity> = mutableMapOf()
    private var entityByProducts : MutableMap<String, ArrayList<StepUnitEntity>> = mutableMapOf()
    private var entityByUnit : MutableMap<String, ArrayList<StepUnitEntity>> = mutableMapOf()
    //    private var materials : MutableMap<String, ArrayList<StepNodeEntity>> = mutableMapOf()
    private var units : MutableMap<String, PlanUnit> = mutableMapOf()
    private var entityByProductRouteStep : MutableMap<String, ArrayList<StepUnitEntity>> = mutableMapOf()
    private var entityByProductRouteStepUnits : MutableMap<String, ArrayList<StepUnitEntity>> = mutableMapOf()
    //    private var unitSteps : MutableMap<String, ArrayList<StepUnitEntity>> = mutableMapOf()
    private var orders : MutableMap<String, ArrayList<OrderEntity>> = mutableMapOf()
    var period = 0

    fun getProductOrders() : MutableMap<String, ArrayList<OrderEntity>> {
        return orders
    }

    fun getProductRouteStepUnits(): Map<String, ArrayList<StepUnitEntity>>{
        return entityByProductRouteStepUnits
    }

    fun getProductRouteSteps(): Map<String, ArrayList<StepUnitEntity>>{
        return entityByProductRouteStep
    }


    fun getProductByUnits(): Map<String, ArrayList<StepUnitEntity>>{
        return entityByUnit
    }

    fun getUnit(id: String): PlanUnit{
        return units[id] ?: throw InputDataNotFoundException("$id 设备没有找到")
    }

    fun getProducts(): Map<String, ArrayList<StepUnitEntity>>{
        return entityByProducts
    }

    fun addUnits(resources: List<PlanUnit>) {
        val ans = mutableMapOf<String, PlanUnit>()
        for (res in resources){
            ans[res.resourceId] = res
        }
        this.units = ans
    }

    /**
     * 添加工序可选设备
     */
    fun addRoutingUnits(stepOnResource: List<StepUnitEntity>){
        val ans = mutableMapOf<String, ArrayList<StepUnitEntity>>()
        val ans2 = mutableMapOf<String, ArrayList<StepUnitEntity>>()
        val ans3 = mutableMapOf<String, ArrayList<StepUnitEntity>>()
        val ans4 = mutableMapOf<String, ArrayList<StepUnitEntity>>()
        for (sor in stepOnResource){
            val key1 = sor.uniqueId
            val key2 = "${sor.productId}*${sor.routeId}*${sor.seqNr}"
            val key3 = sor.productId
            val key4 = sor.unitId
            Util.addToGroup(ans, sor, key1)
            Util.addToGroup(ans2, sor, key2)
            Util.addToGroup(ans3, sor, key3)
            Util.addToGroup(ans4, sor, key4)
        }
        entityByProductRouteStepUnits = ans
        entityByProductRouteStep = ans2
        entityByProducts = ans3
        entityByUnit = ans4
    }
    /**
     * 添加周期
     */
    fun addPeriods(period: Int) {
        this.period = period
    }
    /**
     * 添加订单
     */
    fun addOrders(orders: List<OrderEntity>){
        val ans = mutableMapOf<String, ArrayList<OrderEntity>>()
        for (order in orders){
            val key = order.productId + "_" + order.dueDate
            ans.putIfAbsent(key, arrayListOf())
            ans[key]!!.add(order)
        }
        this.orders = ans
    }

    override fun dispose() {
        entityByProductRouteStepUnits.clear()
        entityByProductRouteStepUnits.clear()
        entityByProductRouteStep.clear()
//        units.clear()
//        stepUnits.clear()
//        unitSteps.clear()
        orders.clear()
//        materials.clear()
    }
}