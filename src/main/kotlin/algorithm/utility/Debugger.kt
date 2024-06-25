package utility

import analysis.evalutor.JSPEvaluator
import catalog.ea.TwoPartitionChromosome
import data.output.Output
import problem.JobSchedulingProblem
import java.io.File

class Debugger {
    companion object {

        fun debugSolution(solution: TwoPartitionChromosome, problem: JobSchedulingProblem, path: String) {
            val output = solution.solution!!.convertToOutput(problem)
            output("C:\\Users\\zp\\project\\shenwei_project\\test_data\\$path.csv", output)
        }

        fun decoderLog(solution: TwoPartitionChromosome, evaluator: JSPEvaluator){
            evaluator.debugRun(solution)
        }

        fun output(path: String, solution: Output) {// CSV文件路径
            try {
                File(path).bufferedWriter().use { writer ->
                    // 写入CSV文件的数据
                    writer.write("MachineName,OperationName,ProcessTime,StartTime")
                    writer.newLine()
                    val map1 = solution.solution
                    for (it in map1){
                        writer.write(it.toString())
                        writer.newLine()
                    }
                }
                println("数据成功写入CSV文件。")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}