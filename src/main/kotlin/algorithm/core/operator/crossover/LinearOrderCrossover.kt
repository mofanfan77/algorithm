package core.operator.crossover

import catalog.ea.CodeSequence
import kotlin.random.Random


/**
 * LOX算子：
 * 1. 交换切片
 * 2. 剩余的基因顺序，由另一个父代的原有顺序决定
 */
class LinearOrderCrossover : CrossOverOperator() {
    override var name = "LOX算子"

    override fun operate(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence> {
        val offspring = ArrayList<CodeSequence>(2)
        val size = father.getSize()
        val cut1 = random.nextInt(size)
        val cut2 = random.nextInt(size)
        val start = minOf(cut1, cut2)
        val end = cut1 + cut2 - start
        // 初始化
        val offspring1 = ArrayList<Int>(size)
        val offspring2 = ArrayList<Int>(size)
        offspring1.addAll(father.generateCodeArray())
        offspring2.addAll(mother.generateCodeArray())
        // 加入固定序列
        for (i in 0 until father.getSize()) {
            if (i !in start..end) {
                if (mother[i] !in offspring1) {
                    offspring1[i] = mother[i]
                }
                if (father[i] !in offspring2) {
                    offspring2[i] = father[i]
                }
            }
        }
        offspring.add(CodeSequence(offspring1))
        offspring.add(CodeSequence(offspring2))
        return offspring
    }
}