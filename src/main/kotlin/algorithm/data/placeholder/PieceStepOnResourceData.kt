package data.placeholder

import core.entity.EntityOnResource
import utility.Util

class PieceStepOnResourceData : DataHolder<EntityOnResource>("工序机器信息") {
    override fun getEntityId(value: EntityOnResource): String {
        return Util.getUniqueCodeOfPieceStepOnResource(value)
    }
}