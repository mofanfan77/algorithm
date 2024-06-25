package catalog.ea

class Gene {

    var upperBound = 0.0
    /**
     * 工序  从1开始
     */
    var sequenceNumber = 0.0
    /**
     * 基因代码 从1开始
     */
    var geneCode = 0.0
    var index = -1.0
    /**
     * 记录分配到的机器
     */
    var assignMachine = 0

    constructor(code: Int, index: Int) {
        this.geneCode = code.toDouble()
        this.index = index.toDouble()
    }

    override fun toString(): String {
        return this.geneCode.toString()
    }
}