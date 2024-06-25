package utility.exception

class ParameterTypeError(var msg: String): Exception(msg)  {
}
class InitializationException(var msg: String): Exception(msg)  {
}