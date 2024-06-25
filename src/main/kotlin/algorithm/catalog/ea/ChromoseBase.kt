package catalog.ea

import catalog.solution.ExploredSolution
import org.apache.xmlbeans.impl.xb.ltgfmt.Code

abstract class ChromoseBase: ExploredSolution() {

    var codeSequences : MutableMap<Int, CodeSequence> = mutableMapOf()


    fun getOSArray(): CodeSequence {
        return this.codeSequences[OS_OPERATION]!!
    }

    fun getMSArray(): CodeSequence {
        return this.codeSequences[MS_OPERATION]!!
    }

    fun getMultiResourceArray(layer: Int): List<CodeSequence>{
        return (MS_OPERATION until MS_OPERATION+layer).map { codeSequences[it]!! }
    }

    fun getPTArray(): CodeSequence{
        return this.codeSequences[PT_OPERATION]!!
    }

    fun getArrayByLayer(layer: Int): CodeSequence{
        return this.codeSequences[layer]!!
    }

    companion object {
        const val PT_OPERATION = 0
        const val OS_OPERATION = 1
        // 方便处理多维资源的情况
        const val MS_OPERATION = 2
    }
}