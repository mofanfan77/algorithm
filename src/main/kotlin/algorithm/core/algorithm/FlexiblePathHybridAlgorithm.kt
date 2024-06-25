package core.algorithm

import catalog.ea.ChromoseBase
import catalog.ea.ChromoseBase.Companion.MS_OPERATION
import catalog.ea.ChromoseBase.Companion.OS_OPERATION
import catalog.ea.ChromoseBase.Companion.PT_OPERATION
import catalog.ea.CodeSequence
import catalog.ea.Population
import catalog.ea.ThreePartitionChromosome
import core.operator.TournamentSelection
import core.operator.crossover.JobBasedCrossover
import core.operator.crossover.MSCrossover
import core.operator.crossover.PrecedenceOperationCrossover
import core.operator.crossover.PriorityExchangeCrossOver
import core.operator.initial.flexiblepath.RandomJobRuleFlexiblePath
import core.operator.initial.flexiblepath.SameSubGroupFlexiblePath
import core.operator.mutation.*
import utility.Util
import java.util.*

class FlexiblePathHybridAlgorithm: JobShopHybridAlgorithm() {

    /**
     * 初始化算子库
     */
    override fun establishPool() {
        // 初始化不同类型算子库
        crossOverPool[MS_OPERATION] = ArrayList()
        crossOverPool[OS_OPERATION] = ArrayList()
        crossOverPool[PT_OPERATION] = ArrayList()
        mutationPool[MS_OPERATION] = ArrayList()
        mutationPool[OS_OPERATION] = ArrayList()
        mutationPool[PT_OPERATION] = ArrayList()
        // 加入算子
        crossOverPool[OS_OPERATION]!!.add(PrecedenceOperationCrossover(random))
        crossOverPool[OS_OPERATION]!!.add(JobBasedCrossover(random))
        crossOverPool[MS_OPERATION]!!.add(MSCrossover(random))
//        crossOverPool[PT_OPERATION]!!.addLazyConstraint(PathSegmentCrossOver())
        crossOverPool[PT_OPERATION]!!.add(PriorityExchangeCrossOver(random))
        mutationPool[OS_OPERATION]!!.add(SwapMutation(random))
        mutationPool[OS_OPERATION]!!.add(TripleSwap(random))
        mutationPool[MS_OPERATION]!!.add(ShiftMutation(random))
        mutationPool[MS_OPERATION]!!.add(MachineMutation(random))
        mutationPool[PT_OPERATION]!!.add(BranchMutation(random))
        selectionPool.add(TournamentSelection(random))
        generatorPool.add(RandomJobRuleFlexiblePath(random))
        generatorPool.add(SameSubGroupFlexiblePath(random))
    }

    override fun crossover() {
        val operatorForOS = selectOperator(crossOverPool[OS_OPERATION]!!)
        val operatorForMS = selectOperator(crossOverPool[MS_OPERATION]!!)
        val operatorForPT = selectOperator(crossOverPool[PT_OPERATION]!!)
        this.selectedOperators.add(operatorForOS)
        this.selectedOperators.add(operatorForMS)
        this.selectedOperators.add(operatorForPT)
        val poolMap = this.population.pool.chunked(2)
        val newChromosome = Collections.synchronizedList(arrayListOf<ChromoseBase>())
        val streamMap = Util.splitBatch(poolMap)
        for (stream in streamMap){
            for (singlePair in stream) {
                if (random.nextDouble() < parameter.crossoverRatio) {
                    val pop1 = singlePair.first()
                    val pop2 = singlePair.last()
                    val offspringOfOS = operatorForOS.run(pop1.getOSArray(), pop2.getOSArray())
                    val offspringOfMS = operatorForMS.run(pop1.getMSArray(), pop2.getMSArray())
                    val offSpringOfPT = operatorForPT.run(pop1.getPTArray(), pop2.getPTArray())
                    val offsprings = ArrayList<ThreePartitionChromosome>()
                    if (offspringOfMS.isEmpty()) {
                        for (codeSequence in offspringOfOS) {
                            offsprings.add(ThreePartitionChromosome(this.problem, codeSequence, CodeSequence(), CodeSequence()))
                        }
                    } else {
                        for (index in offspringOfOS.indices) {
                            val codeSequence = offspringOfOS[index]
                            offsprings.add(
                                ThreePartitionChromosome(
                                    this.problem,
                                    codeSequence,
                                    offspringOfMS[index],
                                    offSpringOfPT[index]
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
        Population.partialUpdate(evaluator, newChromosome)
        this.population.renew(newChromosome)
    }

    override fun mutation() {
        val operatorForOS = selectOperator(mutationPool[OS_OPERATION]!!)
        val operatorForMS = selectOperator(mutationPool[MS_OPERATION]!!)
        val operatorForPT = selectOperator(mutationPool[PT_OPERATION]!!)
        this.selectedOperators.add(operatorForOS)
        this.selectedOperators.add(operatorForMS)
        this.selectedOperators.add(operatorForPT)
        for (i in 1..this.population.getCapacity()) {
            if (random.nextDouble() < parameter.mutationRatio) {
                val offspringOS = operatorForOS.run(population[i - 1].getOSArray())
                val offspringMS = operatorForMS.run(population[i - 1].getMSArray())
                val offspringPT = operatorForPT.run(population[i - 1].getPTArray())
                val newOffSpring: ThreePartitionChromosome = if (offspringMS == null) {
                    ThreePartitionChromosome(this.problem, offspringOS!!, CodeSequence(), CodeSequence())
                } else {
                    ThreePartitionChromosome(this.problem, offspringOS!!, offspringMS, offspringPT!!)
                }
                evaluator.decoder(newOffSpring)
                population[i - 1] = newOffSpring
            }
        }
    }

}