package utility.exception

class CallBackNotFoundException(val msg: String): Exception(msg)  {
}

class CallBackDuplicateException(val msg: String): Exception(msg)  {
}