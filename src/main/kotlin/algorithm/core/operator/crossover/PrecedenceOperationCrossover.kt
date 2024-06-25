package core.operator.crossover

import catalog.ea.CodeSequence
import kotlin.random.Random

/*
 * POX算子：
 * 1. 随机选择一组工件集
 * 2. 工件集内的工件，保留原有位置
 * 3. 工件集外的工件，用另一父代中的位置进行填充
 */
class PrecedenceOperationCrossover(rd: Random) : CrossOverOperator(rd) {
    override var name = "POX算子"
    override fun operate(father: CodeSequence, mother: CodeSequence): ArrayList<CodeSequence> {
        val offspring = ArrayList<CodeSequence>(2)
        // 随机一组工件
        val size = father.maxCode
        val cut = random.nextInt(size)
        val randomJobSet = mutableSetOf<Int>()
        val reOrder = (1..size).shuffled(random)
        randomJobSet.addAll(reOrder.subList(0, cut + 1))
        // 初始化
        val offspring1 = ArrayList<Int>(size)
        val offspring2 = ArrayList<Int>(size)
        offspring1.addAll(father.generateCodeArray())
        offspring2.addAll(mother.generateCodeArray())
        // 加入固定序列
        var pointerFather = 0
        var pointerMother = 0
        for (i in 0 until father.getSize()) {
            if (father[i] !in randomJobSet) { // 在工件集内的位置不变
                while (pointerMother < father.getSize() && mother[pointerMother] in randomJobSet) {
                    pointerMother += 1
                }
                offspring1[i] = mother[pointerMother]
                pointerMother += 1
            }
            if (mother[i] !in randomJobSet) { // 在工件集内的位置不变
                while (pointerFather < father.getSize() && father[pointerFather] in randomJobSet) {
                    pointerFather += 1
                }
                offspring2[i] = father[pointerFather]
                pointerFather += 1
            }
        }
        offspring.add(CodeSequence(offspring1))
        offspring.add(CodeSequence(offspring2))
        return offspring
    }
}

//class PrecedenceOperationCrossOver {
//}