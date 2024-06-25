package utility.exception

class ObjectiveComparionError(var msg: String): Exception(msg)  {
}

class ObjectiveLevelToleranceError(var msg: String): Exception(msg)  {
}

class ObjectiveLevelNotExistError(var msg: String): Exception(msg)  {
}