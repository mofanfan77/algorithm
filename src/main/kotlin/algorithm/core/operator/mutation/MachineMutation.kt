package core.operator.mutation

import utility.Util
import catalog.ea.CodeSequence
import kotlin.random.Random


class MachineMutation(rd: Random): MutationOpeator(rd){
    override var name = "机器变异算子"

    override fun operate(father: CodeSequence) : CodeSequence {
        val size = father.getSize()
        val listInt = ArrayList<Int>()
        listInt.addAll(0 until size)
        val halfSize = (size / 2)
        val neighbors1 = Util.randomMultipleSelect(listInt, halfSize, random)
        val offspring = father.generateCodeArray()
        // 变异
        for (it in neighbors1) {
            val ub = father.getUB(it)
            offspring[it] = random.nextInt(ub)
        }
        return CodeSequence(offspring, father.generateUBArray())
    }
}