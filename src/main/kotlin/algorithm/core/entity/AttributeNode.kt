package core.entity

import utility.enums.TimeUnit
import utility.exception.ObjectAttributeTypeError
import java.time.Duration
import java.time.LocalDateTime

class AttributeNode(var attr: Attributes, var owner: Variable) {
    var updateFlag = true
    var processingFlag = false
    var retry = 0
    var value = attr.value
    var shadowValue: Any? = null

    override fun toString(): String {
        return "$attr||$value||$owner"
    }

    fun getAsDouble(): Double{
        return this.value as Double
    }

    fun getAsBoolean(): Boolean{
        return this.value as Boolean
    }

    fun getAsDuration(): Duration {
        try {
            val unit = owner.getTimeUnit()
            return when (unit){
                TimeUnit.seconds -> Duration.ofSeconds((this.value as Double).toLong())
                TimeUnit.minutes -> Duration.ofMinutes((this.value as Double).toLong())
                TimeUnit.hours -> Duration.ofHours((this.value as Double).toLong())
                TimeUnit.days -> Duration.ofDays((this.value as Double).toLong())
            }
        } catch (e: Exception) {
            throw ObjectAttributeTypeError("类型转换失败, ${this.attr.name} 无法转换成类型 Duration")
        }
    }

    fun getAsLocalDateTime(): LocalDateTime {
        try {
            val unit = owner.getTimeUnit()
            return owner.getTimeZero() + when (unit){
                TimeUnit.seconds -> Duration.ofSeconds((this.value as Double).toLong())
                TimeUnit.minutes -> Duration.ofMinutes((this.value as Double).toLong())
                TimeUnit.hours -> Duration.ofHours((this.value as Double).toLong())
                TimeUnit.days -> Duration.ofDays((this.value as Double).toLong())
            }
        } catch (e: Exception) {
            throw ObjectAttributeTypeError("类型转换失败, ${this.attr.name} 无法转换成类型 LocalDateTime")
        }
    }

    /**
     * 更新数值
     */
    fun update(value: Any) {
        this.shadowValue = value
        this.updateFlag = true
    }

    fun operate(childrenNode: MutableList<AttributeNode>?, flag: Boolean = true) {
        this.receiveSignal()
        if (flag) {
            for (child in childrenNode ?: arrayListOf()) {
                child.sendSignal()
            }
        }
    }

    /**
     * 初始值是否变化
     */
    fun isEmpty(): Boolean {
        return this.value == attr.defaultValue
    }

    /**
     * get attribute value from entity
     */
    @Deprecated("取消无参接口")
    fun compute() {

    }
    /**
     * get attribute value from entity
     */
    fun compute(attn: AttributeNetwork) {
        var a = getValueFromEntity(this.attr.name, attn)
        if (a != null) {
            this.value = a
        }
    }

    private fun getValueFromEntity(name: String, attn: AttributeNetwork): Any? {
        var ans: Any? = null
        val block = this.owner.block
        val relation = this.owner.relation
        val res = this.owner.locate
        if (block?.find(name) == true) {
            ans = block[name]
        } else if (relation?.find(name) == true) {
            ans = relation[name]
        } else if (res?.find(name) == true) {
            ans = res[name]
        } else {
            try {
                val property = EntityOnResource::class.java.getDeclaredField(name)
                property.isAccessible = true
                ans = property.get(relation!!)
            } catch (e: Exception) {
                try {
                    val property2 = JobEntity::class.java.getDeclaredField(name)
                    property2.isAccessible = true
                    ans = property2.get(block!!)
                } catch (e: Exception) {
                    try {
                        val superClass = JobEntity::class.java.superclass
                        val property3 = superClass.getDeclaredField(name)
                        property3.isAccessible = true
                        ans = property3.get(block!!)
                    } catch (e: Exception) {
                        attn.addException(name)
                    }
                }
            }
        }
        return ans
    }

    private fun sendSignal() {
        this.updateFlag = true
    }

    private fun receiveSignal() {
        this.updateFlag = false
    }
}