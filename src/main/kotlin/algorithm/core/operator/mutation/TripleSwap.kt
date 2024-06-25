package core.operator.mutation

import utility.Util
import catalog.ea.CodeSequence
import kotlin.random.Random

class TripleSwap(rd: Random): MutationOpeator(rd){
    override var name = "邻域变异算子"

    override fun operate(father: CodeSequence) : CodeSequence {
        val size = father.getSize()
        val listInt = ArrayList<Int>()
        listInt.addAll(0 until size)
        val neighbors1 = Util.randomMultipleSelect(listInt, 3, random)
        val neighbors2 = neighbors1.shuffled(random)
        val offspring = father.generateCodeArray()
        // 交换
        val swap1 = offspring[neighbors2[0]]
        val swap2 = offspring[neighbors2[1]]
        val swap3 = offspring[neighbors2[2]]
        val swap1Queue = ArrayList<Int>()
        val swap2Queue = ArrayList<Int>()
        val swap3Queue = ArrayList<Int>()
        for (code in 0 until offspring.size){
            if (offspring[code] == swap1){
                swap1Queue.add(code)
            }else if (offspring[code] == swap2){
                swap2Queue.add(code)
            }else if (offspring[code] == swap3){
                swap3Queue.add(code)
            }
        }
        swap1Queue.shuffled(random)
        swap2Queue.shuffled(random)
        swap3Queue.shuffled(random)
        for (code in 0 until minOf(swap1Queue.size, swap2Queue.size, swap3Queue.size)){
            val idx1 = swap1Queue[code]
            val idx2 = swap2Queue[code]
            val idx3 = swap3Queue[code]
            offspring[idx1] = swap2
            offspring[idx2] = swap3
            offspring[idx3] = swap1
        }
        return CodeSequence(offspring)
    }
}