package core.entity

import utility.enums.AttrClassEnum
import utility.enums.AttrTypeEnum
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime

abstract class Attributes(
    val name: String,
    val desc: String,
    var type: AttrTypeEnum,
    var value: Any,
    val group: AttrClassEnum
): Serializable {
    var defaultValue = value
    var reopt = false
    abstract fun clone(): Attributes
    abstract fun clear()
    override fun toString(): String {
        return this.name
    }

    companion object {
        fun getMaxOf(value1: Any?, value2: Any?): Any{
            if (value1 == null || value2 == null){
                throw Exception("传参为空")
            }
            require(value1::class == value2::class) { "数据类型不同, value1 为 ${value1::class}, value2 为 ${value2::class}" }
            return when (value1) {
                is Int -> maxOf(value1, value2 as Int)
                is Double -> maxOf(value1, value2 as Double)
                is Duration -> maxOf(value1, value2 as Duration)
                is LocalDateTime -> maxOf(value1, value2 as LocalDateTime)
                else -> throw Exception("不支持取最值的数据类型 value1 为 ${value1::class}, value2 为 ${value2::class}")
            }
        }

        fun addValue(value1: Any?, value2: Any?): Any {
            if (value1 == null || value2 == null){
                throw Exception("传参为空")
            }
            return when (value1) {
                is Int -> {
                    require(value1::class == value2::class) { "数据类型不同, value1 为 ${value1::class}, value2 为 ${value2::class}" }
                    value1 + value2 as Int
                }
                is Double -> {
                    require(value1::class == value2::class) { "数据类型不同, value1 为 ${value1::class}, value2 为 ${value2::class}" }
                    value1 + value2 as Double
                }
                is Duration -> {
                    val allowed = arrayListOf(LocalDateTime::class, Duration::class)
                    require(value2::class in allowed) { "数据类型无法相加, value1 为 ${value1::class}, value2 为 ${value2::class}" }
                    when (value2) {
                        is Duration -> value1 + value2
                        is LocalDateTime -> value2 + value1
                        else -> {
                            throw Exception("不支持相加的数据类型 value1 为 ${value1::class}, value2 为 ${value2::class}")
                        }
                    }
                }
                is LocalDateTime -> {
                    require(value2::class == Duration::class) { "数据类型无法相加, value1 为 ${value1::class}, value2 为 ${value2::class}" }
                    value1 + value2 as Duration
                }
                else -> {
                    throw Exception("不支持相加的数据类型 value1 为 ${value1::class}, value2 为 ${value2::class}")
                }
            }
        }

        fun equalsTo(value1: Any?, value2: Any?): Boolean {
            if (value1 == null || value2 == null){
                throw Exception("传参为空")
            }
            require(value1::class == value2::class) { "数据类型不同, value1 为 ${value1::class}, value2 为 ${value2::class}" }
            return when (value1) {
                is Int -> value1 == value2
                is Boolean -> value1 == value2
                is String -> value1 == value2
                is Double -> value1 == value2
                else -> throw Exception("不支持比较的数据, value1 为 ${value1::class}, value2 为 ${value2::class}")
            }
        }

        fun greater(value1: Any?, value2: Any?): Boolean{
            if (value1 == null || value2 == null){
                throw Exception("传参为空")
            }
            require(value1::class == value2::class) { "数据类型不同, value1 为 ${value1::class}, value2 为 ${value2::class}" }
            return when (value1) {
                is Int -> value1 > value2 as Int
                is Double -> value1 > value2 as Double
                is Boolean -> value1 && !(value2 as Boolean)
                is Duration -> value1 > value2 as Duration
                is LocalDateTime -> value1 > value2 as LocalDateTime
                else -> throw Exception("不支持比较的数据, value1 为 ${value1::class}, value2 为 ${value2::class}")
            }
        }

        fun minusValue(value1: Any?, value2: Any?): Any {
            if (value1 == null || value2 == null){
                throw Exception("传参为空")
            }
            require(value1::class == value2::class) { "数据类型不同, value1 为 ${value1::class}, value2 为 ${value2::class}" }
            return when (value1) {
                is Int -> value1 - value2 as Int
                is Double -> value1 - value2 as Double
                is Duration -> value1 - value2 as Duration
                is LocalDateTime -> Duration.between(value2 as LocalDateTime,value1)
                else -> throw Exception("不支持想减的数据, value1 为 ${value1::class}, value2 为 ${value2::class}")
            }
        }
    }
}

class TimeAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, LocalDateTime.MIN, group) {
    override fun clone(): TimeAttribute {
        return TimeAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = LocalDateTime.MIN
    }
}

class DurationAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, Duration.ofMinutes(0), group) {
    override fun clone(): DurationAttribute {
        return DurationAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = Duration.ofMinutes(0)
    }
}

class BooleanAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, false, group) {
    constructor(name: String): this(name, "", AttrTypeEnum.Constant, AttrClassEnum.BinVar){}
    constructor(name: String, desc: String, type: AttrTypeEnum, value:Boolean, group: AttrClassEnum) : this(name, desc, type, group) {
        this.value = value
    }
    override fun clone(): BooleanAttribute {
        return BooleanAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = false
    }
}

class IntAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, 0, group) {
    override fun clone(): IntAttribute {
        return IntAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = 0
    }
}

class StrAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, "", group) {
    override fun clone(): StrAttribute {
        return StrAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = ""
    }
}

class DoubleAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, 0.0, group) {
    constructor(name: String): this(name, "", AttrTypeEnum.Constant, AttrClassEnum.DoubleVar){}
    override fun clone(): DoubleAttribute {
        return DoubleAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = 0.0
    }
}

class EntityAttribute(name: String, desc: String, type: AttrTypeEnum, group: AttrClassEnum) :
    Attributes(name, desc, type, Variable(), group) {
    constructor(name: String): this(name, "", AttrTypeEnum.Constant, AttrClassEnum.EntVar){}
    override fun clone(): EntityAttribute {
        return EntityAttribute(name, desc, type, group)
    }

    override fun clear() {
        value = Variable()
    }
}

