package utility.exception

class ObjectAttributeNotFoundException(var msg: String): Exception(msg) {
}
class ObjectAttributeDuplicateException(var msg: String): Exception(msg) {
}
class ObjectAttributeTypeError(var msg: String): Exception(msg) {
}