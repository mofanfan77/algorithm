package data.placeholder

import core.entity.PlanResource
import utility.Util

class ResourceData : DataHolder<PlanResource>("主资源") {
    override fun getEntityId(value: PlanResource): String {
        return Util.getUniqueCodeOfResource(value)
    }
}