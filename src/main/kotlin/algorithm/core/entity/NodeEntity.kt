package core.entity

class NodeEntity : Coordinate() {
    var isOccupied = false
    var canBeRemoved = true
    var isOutbound = false
    var algoNr = 0
    var nodeName = ""

    fun invalidFlag(): Boolean{
        return (isOccupied && !canBeRemoved && !isOutbound)
    }
}