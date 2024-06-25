package core.entity

import problem.BaseProblem
import problem.JobSchedulingProblem
import solver.solverLog
import utility.Util
import utility.enums.AttrTypeEnum
import utility.exception.NodeAggregationException
import utility.exception.VariableDefinationException
import java.time.LocalDateTime

class AttributeNetwork(var problem: BaseProblem, private var result: MutableMap<PlanEntity, Variable>,
                       private var retryLimit: Double = 5.0) {
    private var constraintGenerator = problem.generator
    private var network: MutableMap<AttributeNode, ArrayList<AttributeNode>> = mutableMapOf()
    private var networkReversed: MutableMap<AttributeNode, ArrayList<AttributeNode>> = mutableMapOf()
    private var nodeChain = mutableListOf<AttributeNode>()
    private var subSequences = mutableMapOf<String, SubVariableSequence>()
    private var exceptionMessagePool = HashSet<String>()
    private var activeStatus = true

    /**
     * 节点网络图计算
     */
    private fun calcNetwork() {
        if (activeStatus) {
            val start = System.currentTimeMillis()
            solverLog.debug("网络计算开始时间 : {} at {}", LocalDateTime.now(), start)
            for (source in nodeChain) {
                this.interpret(source)
            }
            if (exceptionMessagePool.isNotEmpty()) {
                throw NodeAggregationException("获取属性值失败\n:${exceptionMessagePool.joinToString(" ; ")} \n变量在实例中无法找到")
            }
            val end = System.currentTimeMillis()
            activeStatus = false
            solverLog.debug("网络计算结束时间 : {} at {}", LocalDateTime.now(), end)
            solverLog.debug("用时 ${end - start} ms")
        }
    }

    /**
     * 计算核心
     */
    fun run() {
        calcNetwork()
        generateSubSequence()
        updateReoptNode()
        calcNetwork()
    }

    /**
     * 生成变量组进行重优化
     */
    private fun generateSubSequence() {
        val groups = this.constraintGenerator.getGroups()
        if (groups.isNotEmpty()) {
            val tempSort = result.values.sortedBy { it.getStartTime() }
            if (groups.isNotEmpty()) {
                for (temp in tempSort) {
                    for ((groupName, v) in groups) {
                        val key = v.map { temp[it].value.toString() }.joinToString("&")
                        this.subSequences.putIfAbsent(key, SubVariableSequence(key, groupName))
                        this.subSequences[key]!!.connect(temp)
                    }
                }
            }
            activeStatus = true
        }
    }

    /**
     * 更新重优化的节点网络
     */
    private fun updateReoptNode() {
        for (reopt in constraintGenerator.SecondaryIterator()) {
            val name = reopt.groupName
            for ((_, sequence) in subSequences) {
                if (sequence.groupId == name) {
                    for (entity in sequence.taskSequence) {
                        reopt.calc(entity)
                    }
                }
            }
        }
        activeStatus = true
    }

    private fun interpret(source: AttributeNode, msg: String = "") {
        // dependent nodes
        if (source.updateFlag) {
            if (source.processingFlag) {
                solverLog.error(msg)
                throw VariableDefinationException("$source variable caught a cyclic dependency in the calculation chain!")
            }
            source.processingFlag = true
            val childrenNode = network[source]
            val parentNode = networkReversed[source]?.filter { it.updateFlag }
            if (parentNode?.isNotEmpty() == true) {
                for (it in parentNode) {
                    var newMsg = msg
                    if (it.owner != source.owner) {
                        newMsg += "$source to $it \n"
                    }
                    interpret(it, newMsg)
                }
            }
            val flag = renew(source)
            source.operate(childrenNode, flag)
            source.processingFlag = false
        }
    }

    private fun renew(attrNode: AttributeNode): Boolean {
        var ans = true
        if (attrNode.retry < retryLimit) {
            attrNode.retry += 1
            val attr = attrNode.attr
            val variable = attrNode.owner
            if (attrNode.shadowValue != null) {
                attrNode.value = attrNode.shadowValue!!
            } else {
                val type = attr.type
                if (type == AttrTypeEnum.Constant) {
                    attrNode.compute(this)
                } else {
                    val constr = this.constraintGenerator.getConstr(attr)
                    constr?.let { con ->
                        val orig = attrNode.value
                        con.calc(variable)
                        val after = attrNode.value
                        if (orig != after) {
                            ans = false
                        }
                    }
                }
            }
        }
        return ans
    }

    fun addException(name: String) {
        exceptionMessagePool.add(name)
    }

    /**
     * initialize attributes
     */
    fun initializeAttributes() {
        for (tb in result.values) {
            for (it in constraintGenerator.getAttributes()) {
                val result = tb.create(it)
                result?.let { res ->
                    network.putIfAbsent(res, arrayListOf())
                    networkReversed.putIfAbsent(res, arrayListOf())
                }
            }
        }
    }

    /**
     *  generate variable relation maps
     *  generate variable topo-order
     *  including secondary constraints
     */
    fun generateVariableMap() {
        for (constr in constraintGenerator.InitialIterator()) {
            constr.connect(this, result)
        }
//        for (constr in constraintGenerator.SecondaryIterator()) {
//            constr.connect(this, result)
//        }
//        nodeChain = Util.loopTopoSort(networkReversed)
        nodeChain = Util.topoSort(network)
    }

    /**
     * 变量网络 用于更新变量值
     */
    fun connect(after: AttributeNode, prev: AttributeNode) {
        network[prev]?.add(after)
        networkReversed[after]?.add(prev)
    }

    fun connectWithPrior(element: Variable, targetAttr: Attributes, sourceAttr: Attributes) {
        val prevOperations = element.priorVariables
        for (newElement in prevOperations) {
            this.connect(element[targetAttr], newElement[sourceAttr])
        }
    }

    fun connectWithSubseq(element: Variable, source: Attributes, target: Attributes) {
        val nextWIPs = element.subsequenVariables
        for (newElement in nextWIPs) {
            this.connect(element[source], newElement[target])
        }
    }
}