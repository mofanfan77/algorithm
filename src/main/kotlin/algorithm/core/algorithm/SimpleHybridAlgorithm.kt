package core.algorithm

import catalog.ea.ChromoseBase
import catalog.ea.Population
import catalog.ea.SinglePartitionChromosome
import core.operator.TournamentSelection
import core.operator.initial.routing.SingleBestJobRule
import core.operator.initial.routing.SingleRandomJobRule
import problem.CraneAssignProblem
import utility.Util
import utility.annotation.Ignored
import java.util.*

class SimpleHybridAlgorithm : HybridAlgorithm(){
    lateinit var problem: CraneAssignProblem
    override fun establishPool() {
        // 初始化不同类型算子库
        crossOverPool[ChromoseBase.OS_OPERATION] = ArrayList()
        mutationPool[ChromoseBase.OS_OPERATION] = ArrayList()
        // 加入算子
//        crossOverPool[ChromoseBase.OS_OPERATION]!!.add(PrecedenceOperationCrossover())
//        mutationPool[ChromoseBase.OS_OPERATION]!!.add(SwapMutation())
        selectionPool.add(TournamentSelection(random))
        generatorPool.add(SingleRandomJobRule(random))
        generatorPool.add(SingleBestJobRule(random))
    }

    override fun crossover() {
        val operatorForOS = selectOperator(crossOverPool[ChromoseBase.OS_OPERATION]!!)
        this.selectedOperators.add(operatorForOS)
        val poolMap = this.population.pool.chunked(2)
        val newChromosome = Collections.synchronizedList(arrayListOf<ChromoseBase>())
        val streamMap = Util.splitBatch(poolMap)
        for (stream in streamMap){
            for (singlePair in stream) {
                if (random.nextDouble() < parameter.crossoverRatio) {
                    val pop1 = singlePair.first()
                    val pop2 = singlePair.last()
                    val offspringOfOS = operatorForOS.run(pop1.getOSArray(), pop2.getOSArray())
                    val offsprings = ArrayList<SinglePartitionChromosome>()
                    for (index in offspringOfOS.indices) {
                        val codeSequence = offspringOfOS[index]
                        offsprings.add(
                            SinglePartitionChromosome(
                                this.problem,
                                codeSequence
                            )
                        )
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
        val operatorForOS = selectOperator(mutationPool[ChromoseBase.OS_OPERATION]!!)
        this.selectedOperators.add(operatorForOS)
        for (i in 1..this.population.getCapacity()) {
            if (random.nextDouble() < parameter.mutationRatio) {
                val offspringOS = operatorForOS.run(population[i - 1].getOSArray())
                val newOffSpring = SinglePartitionChromosome(this.problem, offspringOS!!)
                evaluator.decoder(newOffSpring)
                population[i - 1] = newOffSpring
            }
        }
    }

    @Ignored
    override fun addInitSolution() {
        return
    }

    override fun generate(number: Double): ArrayList<ChromoseBase> {
        val ans = ArrayList<ChromoseBase>()
        for (i in 1..number.toInt()) {
            val operator = selectOperator(generatorPool)
            val toAdd = operator.operate(this.problem)
            ans.add(toAdd)
        }
        return ans
    }

    @Ignored
    override fun initialEvaluate() {
        return
    }
}