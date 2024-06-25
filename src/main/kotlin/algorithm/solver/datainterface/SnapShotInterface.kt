package solver.datainterface

interface SnapShotInterface {
    /**
     * 导出序列化文件
     */
    fun exportData(path: String)
    fun loadData(path: String)
}