package data.placeholder

abstract class DataHolder<T>(var name: String?) {
    // 业务模型 -> 业务实体 映射
    private val propertyMap = mutableMapOf<String, T>()

    // 算法模型 <-> 业务模型 code
    private val algo2modelMap = mutableMapOf<String, String>()
    private val model2AlgoMap = mutableMapOf<String, String>()

    fun getAll(): List<T>{
        return propertyMap.values.toList()
    }

    fun getMaskMap(): MutableMap<String, String> {
        return algo2modelMap
    }

    fun getReversedMaskMap(): MutableMap<String, String> {
        return model2AlgoMap
    }

    fun getPropertyMap(): MutableMap<String, T> {
        return propertyMap
    }

    fun addMask(key: String, value: T) {
        val value2 = getEntityId(value)
        this.algo2modelMap[key] = value2
        this.model2AlgoMap[value2] = key
    }

    fun addEntity(value: T) {
        val key = getEntityId(value)
        this.propertyMap[key] = value
    }

    fun getEntity(key: String): T? {
        return this.propertyMap[key]
    }

    fun getRealCode(key:String): String?{
        return this.algo2modelMap[key]
    }

    fun getMaskCode(key:String): String?{
        return this.model2AlgoMap[key]
    }

    abstract fun getEntityId(value: T): String

    fun reset() {
        this.propertyMap.clear()
        this.algo2modelMap.clear()
        this.model2AlgoMap.clear()
    }

    fun getPropertySize(): Int {
        return propertyMap.size
    }
}