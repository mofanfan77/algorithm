package utility.exception

class InputDataNotFoundException(val msg: String): Exception(msg)  {
}

class InputDataOverFlowError(val msg: String): Exception(msg)  {
}

class InputDataValidationException(val msg: String): Exception(msg){}