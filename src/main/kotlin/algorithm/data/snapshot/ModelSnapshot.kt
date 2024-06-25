package data.snapshot

import utility.exception.ModelSnapShotExportError
import utility.exception.ModelSnapShotImportError
import java.io.*

open class ModelSnapshot : Serializable {
    companion object {
        fun serialize(container: ModelSnapshot, filename: String) {
            try {
                FileOutputStream(filename).use { fileOut ->
                    ObjectOutputStream(fileOut).use { out ->
                        out.writeObject(container)
                    }
                }
            } catch (e: Exception) {
                throw ModelSnapShotExportError("模型导出失败, ${e.stackTraceToString()}")
            }
        }

        fun deserialize(filename: String): ModelSnapshot? {
            try {
                FileInputStream(filename).use { fileIn ->
                    ObjectInputStream(fileIn).use { `in` ->
                        val rod = `in`.readObject()
                        if (rod is ModelSnapshot) {
                            return rod
                        }
                    }
                }
            } catch (e: Exception) {
                throw ModelSnapShotImportError("模型导入失败, ${e.stackTraceToString()}")
            }
            return null
        }
    }
}