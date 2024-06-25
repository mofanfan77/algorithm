package utility.exception

class ConstrDuplicateException(val msg: String): Exception(msg)  {
}

class ConstrExecutionException(val msg: String): Exception(msg) {}