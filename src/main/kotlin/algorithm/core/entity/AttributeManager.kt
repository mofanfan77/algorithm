package core.entity

import solver.solverLog
import utility.Util
import utility.enums.AttrTypeEnum
import utility.exception.VariableDefinationException
import utility.exception.VariableGroupDuplicateException

/**
 * 变量管理
 */
open class AttributeManager {
    private val attributeNode = mutableMapOf<String, Attributes>()
    private var graph = mutableMapOf<Attributes, ArrayList<Attributes>>()
    private val groups = mutableMapOf<String, Array<Attributes>>()
    fun addVariable(variable: Attributes) {
        this.attributeNode[variable.name] = variable
    }

    fun getAttributes(): List<Attributes> {
        return attributeNode.values.toList()
    }

    fun mapAttribute(varName: String): Attributes {
        val value: Attributes
        try {
            value = this.attributeNode[varName]!!
        } catch (e: NullPointerException) {
            throw VariableDefinationException("$varName 该变量不存在")
        }
        return value
    }

    fun mapAttributes(vararg varName: String): Array<Attributes> {
        return Array(varName.size) { mapAttribute(varName[it]) }
    }

    fun checkFeasibility() {
        val inDegrees = Util.getIndegreeMap(graph)
        // 变量检查
        for (it in attributeNode.values) {
            when (it.type) {
                AttrTypeEnum.Constant -> if (it !in graph) {
                    solverLog.warn("这个属性未在约束中出现 ${it.name}")
                }

                AttrTypeEnum.Calculation -> if (inDegrees.getOrDefault(it, 0) < 1) {
                    solverLog.warn("这个计算属性 ${it.name}没有依赖")
                }

                AttrTypeEnum.Decision -> if (graph.getOrDefault(it, arrayListOf()).size == 0) {
                    solverLog.warn("这个决策变量 ${it.name}没有生效")
                }
            }
        }
    }

    operator fun get(key: String): Attributes {
        require(key in attributeNode) { "查询的属性 $key 未在属性列表中初始化， 请修正" }
        return this.attributeNode[key]!!
    }

    fun addGroup(name: String, targets: Array<Attributes>) {
        if (name in this.groups) {
            throw VariableGroupDuplicateException("该变量分组已事先定义，请使用别的分组名称${name}")
        }
        this.groups[name] = targets
    }

    fun getGroups(): Map<String, Array<Attributes>> {
        return this.groups
    }
}