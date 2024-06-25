package core.algorithm

import catalog.ea.ChromoseBase
import catalog.ea.ChromoseBase.Companion.MS_OPERATION
import catalog.ea.ChromoseBase.Companion.OS_OPERATION
import catalog.ea.CodeSequence
import catalog.ea.Population.Companion.partialUpdate
import catalog.ea.TwoPartitionChromosome
import core.operator.TournamentSelection
import core.operator.crossover.JobBasedCrossover
import core.operator.crossover.MSCrossover
import core.operator.crossover.PrecedenceOperationCrossover
import core.operator.initial.*
import core.operator.mutation.MachineMutation
import core.operator.mutation.ShiftMutation
import core.operator.mutation.SwapMutation
import core.operator.mutation.TripleSwap
import utility.Util.Companion.splitBatch
import java.util.*

class SinglePathHybridAlgorithm : JobShopHybridAlgorithm() {


    /**
     * 基于最优解进行局部搜索
     */
    override fun localSearch() {
        searcher.run(this, evaluator, problem, parameter)
    }

    /**
     * 交叉类算子（需要两个pop）
     */
    override fun crossover() {
        val operatorForOS = selectOperator(crossOverPool[OS_OPERATION]!!)
        val operatorForMS = selectOperator(crossOverPool[MS_OPERATION]!!)
        this.selectedOperators.add(operatorForOS)
        this.selectedOperators.add(operatorForMS)
        val poolMap = this.population.pool.chunked(2)
        val newChromosome = Collections.synchronizedList(arrayListOf<ChromoseBase>())
        val streamMap = splitBatch(poolMap)
        for (stream in streamMap){
            for (singlePair in stream) {
                if (random.nextDouble() < parameter.crossoverRatio) {
                    val pop1 = singlePair.first()
                    val pop2 = singlePair.last()
                    val offspringOfOS = operatorForOS.run(pop1.getOSArray(), pop2.getOSArray())
                    val offspringOfMS = operatorForMS.run(pop1.getMSArray(), pop2.getMSArray())
                    val offsprings = ArrayList<TwoPartitionChromosome>()
                    if (offspringOfMS.isEmpty()) {
                        for (codeSequence in offspringOfOS) {
                            offsprings.add(TwoPartitionChromosome(this.problem, codeSequence, CodeSequence()))
                        }
                    } else {
                        for (index in offspringOfOS.indices) {
                            val codeSequence = offspringOfOS[index]
                            offsprings.add(
                                TwoPartitionChromosome(
                                    this.problem,
                                    codeSequence,
                                    offspringOfMS[index]
                                )
                            )
                        }
                    }
                    newChromosome.addAll(offsprings)
                } else {
                    newChromosome.addAll(singlePair)
                }
            }
        }
        partialUpdate(evaluator, newChromosome)
        this.population.renew(newChromosome)
    }

    /**
     * 变异类算子（需要一个pop）
     */
    override fun mutation() {
        val operatorForOS = selectOperator(mutationPool[OS_OPERATION]!!)
        val operatorForMS = selectOperator(mutationPool[MS_OPERATION]!!)
        this.selectedOperators.add(operatorForOS)
        this.selectedOperators.add(operatorForMS)
        for (i in 1..this.population.getCapacity()) {
            if (random.nextDouble() < parameter.mutationRatio) {
                val offspringOS = operatorForOS.run(population[i - 1].getOSArray())
                val offspringMS = operatorForMS.run(population[i - 1].getMSArray())
                val newOffSpring: TwoPartitionChromosome = if (offspringMS == null) {
                    TwoPartitionChromosome(this.problem, offspringOS!!, CodeSequence())
                } else {
                    TwoPartitionChromosome(this.problem, offspringOS!!, offspringMS)
                }
                evaluator.decoder(newOffSpring)
                population[i - 1] = newOffSpring
            }
        }
    }

    /**
     * 初始化算子库
     */
    override fun establishPool() {
        // 初始化不同类型算子库
        crossOverPool[MS_OPERATION] = ArrayList()
        crossOverPool[OS_OPERATION] = ArrayList()
        mutationPool[MS_OPERATION] = ArrayList()
        mutationPool[OS_OPERATION] = ArrayList()
        // 加入算子
        crossOverPool[OS_OPERATION]!!.add(PrecedenceOperationCrossover(random))
        crossOverPool[OS_OPERATION]!!.add(JobBasedCrossover(random))
        crossOverPool[MS_OPERATION]!!.add(MSCrossover(random))
        mutationPool[OS_OPERATION]!!.add(SwapMutation(random))
        mutationPool[OS_OPERATION]!!.add(TripleSwap(random))
        mutationPool[MS_OPERATION]!!.add(ShiftMutation(random))
        mutationPool[MS_OPERATION]!!.add(MachineMutation(random))
        selectionPool.add(TournamentSelection(random))
        generatorPool.add(LeastJobRule(random))
        generatorPool.add(LeaseChangeGroupingRule(random))
        generatorPool.add(RandomJobRule(random))
        generatorPool.add(LeastJobGroupingRule(random))
        generatorPool.add(EarlyTimeGroupingRule(random))
    }

}