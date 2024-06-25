package core.entity.supplychain

import core.entity.AlgoObject

class OrderEntity : AlgoObject(){
    var orderId = ""
    var productId = ""
    var quantity = 1.0
    var dueDate = 1
}