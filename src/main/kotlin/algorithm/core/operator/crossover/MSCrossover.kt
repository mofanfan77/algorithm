package core.operator.crossover

import catalog.ea.CodeSequence
import kotlin.random.Random

class MSCrossover(rd: Random) : CrossOverOperator(rd) {
    override var name = "MS交叉算子"

    override fun operate(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence> {
        val offspring = ArrayList<CodeSequence>(2)
        // 随机一组工件
        val size = father.getSize()
        val cut1 = random.nextInt(size)
        val cut2 = random.nextInt(size)
        val start = minOf(cut1, cut2)
        val end = maxOf(cut1, cut2)
        // 初始化
        val offspring1 = ArrayList<Int>(size)
        val offspring2 = ArrayList<Int>(size)
        offspring1.addAll(father.generateCodeArray())
        offspring2.addAll(mother.generateCodeArray())
        // 加入固定序列
        for (i in 0 until father.getSize()) {
            if (i < start || i > end) { // 在工件集内的位置不变
                continue
            } else { // 不在工件集内的位置不变
                offspring1[i] = mother[i]
                offspring2[i] = father[i]
            }
        }
        offspring.add(CodeSequence(offspring1, father.generateUBArray()))
        offspring.add(CodeSequence(offspring2, mother.generateUBArray()))
        return offspring
    }
}
