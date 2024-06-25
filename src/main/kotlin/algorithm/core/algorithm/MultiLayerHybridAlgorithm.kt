package core.algorithm

import catalog.ea.ChromoseBase
import catalog.ea.ChromoseBase.Companion.MS_OPERATION
import catalog.ea.ChromoseBase.Companion.OS_OPERATION
import catalog.ea.ChromoseBase.Companion.PT_OPERATION
import catalog.ea.CodeSequence
import catalog.ea.MultiPartitionChromosome
import catalog.ea.Population.Companion.partialUpdate
import core.operator.TournamentSelection
import core.operator.crossover.*
import core.operator.initial.multiresource.RandomJobRuleMultipleResource
import core.operator.mutation.*
import utility.Util
import java.util.*

class MultiLayerHybridAlgorithm(private var layer: Int, private var pathFlag: Boolean) : JobShopHybridAlgorithm() {

    /**
     * 初始化算子库
     */
    override fun establishPool() {
        // 初始化不同类型算子库
        crossOverPool[OS_OPERATION] = arrayListOf()
        mutationPool[OS_OPERATION] = arrayListOf()
        for (l in MS_OPERATION until this.layer + MS_OPERATION) {
            crossOverPool[l] = arrayListOf()
            mutationPool[l] = arrayListOf()
        }
        // 加入算子
        crossOverPool[OS_OPERATION]!!.add(PrecedenceOperationCrossover(random))
        crossOverPool[OS_OPERATION]!!.add(JobBasedCrossover(random))
        mutationPool[OS_OPERATION]!!.add(SwapMutation(random))
        mutationPool[OS_OPERATION]!!.add(TripleSwap(random))
        for (l in MS_OPERATION until this.layer + MS_OPERATION) {
            crossOverPool[l]!!.add(MSCrossover(random))
            mutationPool[l]!!.add(ShiftMutation(random))
            mutationPool[l]!!.add(MachineMutation(random))
        }
        // 如果有PT
        if (pathFlag) {
            crossOverPool[PT_OPERATION] = arrayListOf()
            mutationPool[PT_OPERATION] = arrayListOf()
            crossOverPool[PT_OPERATION]!!.add(PriorityExchangeCrossOver(random))
            mutationPool[PT_OPERATION]!!.add(BranchMutation(random))
        }
        // 生成新的
        selectionPool.add(TournamentSelection(random))
        generatorPool.add(RandomJobRuleMultipleResource(random, this.layer, this.pathFlag))
//        generatorPool.add(SameSubGroupFlexiblePath(this.layer, this.pathFlag))
    }

    override fun crossover() {
        val start = if (pathFlag) PT_OPERATION else OS_OPERATION
        val operators = Array<CrossOverOperator>(this.layer + MS_OPERATION - start) { _ -> MSCrossover(random) }
        for (l in start until this.layer + MS_OPERATION) {
            val temp = selectOperator(crossOverPool[l]!!)
            this.selectedOperators.add(temp)
            operators[l - start] = temp
        }
        val poolMap = this.population.pool.chunked(2)
        val newChromosome = Collections.synchronizedList(arrayListOf<ChromoseBase>())
        val streamMap = Util.splitBatch(poolMap)
        for (stream in streamMap){
            for (singlePair in stream) {
                if (random.nextDouble() < parameter.crossoverRatio) {
                    val pop1 = singlePair.first()
                    val pop2 = singlePair.last()
                    val arrays1 = Array(this.layer + MS_OPERATION - start) { _ -> CodeSequence() }
                    val arrays2 = Array(this.layer + MS_OPERATION - start) { _ -> CodeSequence() }
                    for (l in start until this.layer + MS_OPERATION) {
                        var res = operators[l - start].run(pop1.getArrayByLayer(l), pop2.getArrayByLayer(l))
                        arrays1[l - start] = res.first()
                        arrays2[l - start] = res.last()
                    }
                    val offsprings = ArrayList<MultiPartitionChromosome>()
                    offsprings.add(MultiPartitionChromosome(this.problem, arrays1, pathFlag))
                    offsprings.add(MultiPartitionChromosome(this.problem, arrays2, pathFlag))
                    newChromosome.addAll(offsprings)
                } else {
                    newChromosome.addAll(singlePair)
                }
            }
        }
        partialUpdate(evaluator, newChromosome)
        this.population.renew(newChromosome)
    }

    override fun mutation() {
        val start = if (pathFlag) PT_OPERATION else OS_OPERATION
        val operators = Array<MutationOpeator>(this.layer + MS_OPERATION - start) { _ -> SwapMutation(random) }
        for (l in start until this.layer + MS_OPERATION) {
            val temp = selectOperator(mutationPool[l]!!)
            this.selectedOperators.add(temp)
            operators[l - start] = temp
        }
        for (i in 1..this.population.getCapacity()) {
            if (random.nextDouble() < parameter.mutationRatio) {
                val arrays1 = Array(this.layer + MS_OPERATION - start) { _ -> CodeSequence() }
                for (l in start..this.layer + OS_OPERATION) {
                    val operator = operators[l - start]
                    arrays1[l - start] = operator.run(population[i - 1].getArrayByLayer(l))!!
                }
                val newOffSpring = MultiPartitionChromosome(this.problem, arrays1, pathFlag)
                evaluator.decoder(newOffSpring)
                population[i - 1] = newOffSpring
            }
        }
    }
}