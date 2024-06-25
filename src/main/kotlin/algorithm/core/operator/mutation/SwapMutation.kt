package core.operator.mutation

import catalog.ea.CodeSequence
import java.util.*
import kotlin.random.Random

class SwapMutation(rd: Random): MutationOpeator(rd){
    override var name = "交换变异算子"

    override fun operate(father: CodeSequence) : CodeSequence {
        val size = father.getSize()
        val cut1 = random.nextInt(size)
        val cut2 = random.nextInt(size)
        // 对应位置的工件进行交换
        val offspring = father.generateCodeArray()
        val swap1 = offspring[cut1]
        val swap2 = offspring[cut2]
        val swap1Queue = ArrayList<Int>()
        val swap2Queue = ArrayList<Int>()
        for (code in 0 until offspring.size){
            if (offspring[code] == swap1){
                swap1Queue.add(code)
            }else if (offspring[code] == swap2){
                swap2Queue.add(code)
            }
        }
        swap1Queue.shuffled(random)
        swap2Queue.shuffled(random)
        for (code in 0 until minOf(swap1Queue.size, swap2Queue.size)){
            val idx1 = swap1Queue[code]
            val idx2 = swap2Queue[code]
            offspring[idx1] = swap2
            offspring[idx2] = swap1
        }
        return CodeSequence(offspring)
    }
}