package data.output

import data.BaseData

open class Output {
    var solution = ArrayList<BaseData>()

    fun updateSolution(value : ArrayList<BaseData>){
        solution = value
    }
}