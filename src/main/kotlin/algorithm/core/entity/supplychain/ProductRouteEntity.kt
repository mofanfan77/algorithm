package core.entity.supplychain

import core.entity.AlgoObject

class ProductRouteEntity : AlgoObject() {
    var productId = ""
    var productRouteId = ""
    var steps = arrayListOf<String>()
}
