package utility.exception

class VariableDefinationException(var msg: String): Exception(msg) {
}

class VariableGroupDefinationException(var msg: String): Exception(msg) {
}

class VariableGroupDuplicateException(var msg: String): Exception(msg) {
}