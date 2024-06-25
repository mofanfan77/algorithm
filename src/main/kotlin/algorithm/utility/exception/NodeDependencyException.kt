package utility.exception

class NodeDependencyException(var msg: String): Exception(msg) {
}

class NodeAggregationException(var msg: String): Exception(msg) {

}

class NodeNotExistException(var msg: String): Exception(msg) {
}