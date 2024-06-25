package core.entity

import utility.exception.ObjectAttributeDuplicateException
import utility.exception.ObjectAttributeNotFoundException
import java.io.Serializable

abstract class AlgoObject : Serializable {
    private var objectAttributes = mutableMapOf<String, Any>()

    operator fun get(key: String): Any{
        return objectAttributes[key] ?: throw ObjectAttributeNotFoundException("${key}属性在变量中不存在")
    }

    operator fun set(key: String, value: Any){
        if (key in objectAttributes){
            throw ObjectAttributeDuplicateException("${key}属性在变量中已经被定义，请换变量名")
        }
        objectAttributes[key] = value
    }

    fun find(name: String): Boolean{
        return name in objectAttributes
    }
}