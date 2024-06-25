package core.entity

import java.io.Serializable
import kotlin.math.abs

open class Coordinate : Serializable{
    var x = 0.0
    var y = 0.0

    companion object {
        fun getOneDimDistance(l1: Coordinate, l2: Coordinate) : Double{
            return abs(l1.x - l2.x)
        }
    }
}