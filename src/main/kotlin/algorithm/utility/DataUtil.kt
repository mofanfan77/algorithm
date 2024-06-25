package utility

import data.DataGroup
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import core.entity.*
import java.io.File

class DataUtil {
    val mapper: ObjectMapper
    var newObject: DataGroup = DataGroup()

    init{
        mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
    }

    constructor(){}
    constructor(path:String){
        newObject = mapper.readValue(File(path), DataGroup::class.java)
    }

    /**
     * 导入json字符串
     */
    fun loadFromJson(jsonData: String){
        newObject = mapper.readValue(jsonData, DataGroup::class.java)
    }

    fun getPlanEntityFromJson(): ArrayList<PlanEntity> {
        val ans = ArrayList<PlanEntity>()
        for (it in newObject.piecestep){
            val object2 = mapper.readValue(it, JobEntity::class.java)
            ans.add(object2)
        }
        return ans
    }

    fun getPlanResourceFromJson(): ArrayList<PlanResource> {
        val ans = ArrayList<PlanResource>()
        for (it in newObject.resource){
            val object2 = mapper.readValue(it, PlanResource::class.java)
            ans.add(object2)
        }
        return ans
    }

    fun getResourceEntityFromJson(): ArrayList<EntityOnResource> {
        val ans = ArrayList<EntityOnResource>()
        for (it in newObject.pieceStepOnResource){
            val object2 = mapper.readValue(it, EntityOnResource::class.java)
            ans.add(object2)
        }
        return ans
    }

    fun getCalendarFromJson(): ArrayList<PlanCalendar> {
        val ans = ArrayList<PlanCalendar>()
        for (it in newObject.calendar){
            val object2 = mapper.readValue(it, PlanCalendar::class.java)
            ans.add(object2)
        }
        return ans
    }

    fun getViceFromJson(): ArrayList<PlanSubResource> {
        val ans = ArrayList<PlanSubResource>()
        for (it in newObject.vices){
            val object2 = mapper.readValue(it, PlanSubResource::class.java)
            ans.add(object2)
        }
        return ans
    }
}