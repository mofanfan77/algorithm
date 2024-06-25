package utility.exception

class UtilSortException(var msg: String): Exception(msg) {
}

class ModelSnapShotExportError(var msg: String): Exception(msg) {}
class ModelSnapShotImportError(var msg: String): Exception(msg) {}
