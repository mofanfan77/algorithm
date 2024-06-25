package data.placeholder

import core.entity.PlanEntity
import utility.Util

class EntityData : DataHolder<PlanEntity>("工件工序") {
    var pieceCounter = mutableMapOf<Int, Int>()
    var relationMap = mutableMapOf<String, Int>()

    override fun getEntityId(value: PlanEntity): String {
        return Util.getUniqueCodeOfEntity(value)
    }
}