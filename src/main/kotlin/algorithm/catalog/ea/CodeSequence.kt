package catalog.ea

class CodeSequence {
    var code = ArrayList<Gene>()
    var owner : ChromoseBase? = null
    var maxCode = 0

    constructor()
    constructor(intList: ArrayList<Int>) {
        val newCode = ArrayList<Gene>()
        var codeUB = 0
        for (index in 0 until intList.size){
            val i = intList[index]
            newCode.add(Gene(i, index))
            codeUB = maxOf(codeUB, i)
        }
        code = newCode
        this.maxCode = codeUB
    }

    constructor(intList: ArrayList<Int>, upperBound: ArrayList<Int>) {
        val newCode = ArrayList<Gene>()
        var codeUB = 0
        for (index in 0 until upperBound.size){
            val i = upperBound[index]
            val gene = Gene(intList[index], index)
            gene.upperBound = i.toDouble()
            codeUB = maxOf(codeUB, intList[index])
            newCode.add(gene)
        }
        code = newCode
        this.maxCode = codeUB
    }

    /**
     * 返回对应的基因代码
     */
    operator fun get(index: Int): Int {
        return code[index].geneCode.toInt()
    }

    /**
     * 生成数组序列
     */
    fun generateCodeArray(): ArrayList<Int> {
        return ArrayList(code.map { it.geneCode.toInt() }.toList())
    }

    fun generateUBArray(): ArrayList<Int> {
        return ArrayList(code.map { it.upperBound.toInt() }.toList())
    }

    fun getUB(index: Int): Int {
        return code[index].upperBound.toInt()
    }

    fun getSize(): Int {
        return code.size
    }

    fun clone(): CodeSequence {
        return CodeSequence(this.generateCodeArray(), this.generateUBArray())
    }
}