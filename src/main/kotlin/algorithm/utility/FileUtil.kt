package utility

//import solver.solverLog
import java.io.*

class FileUtil {
    companion object {
        fun writeToCsv(dataList: ArrayList<String>, path: String) {
            val file = FileOutputStream(path)
            try {
                val pw = OutputStreamWriter(file)
                val bw = BufferedWriter(pw)
                bw.write("resourceId,jobNr,prefixStart,prefixEnd,start,end")
                bw.newLine()
                for (it in dataList) {
                    bw.write(it)
                    bw.newLine()
                    println(it)
                }
                bw.flush()
            } catch (e: Exception) {
//                solverLog.error(e.stackTraceToString())
//                solverLog.error("输出发生错误")
            }
        }
    }
}