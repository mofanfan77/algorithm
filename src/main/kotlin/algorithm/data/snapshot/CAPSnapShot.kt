package data.snapshot

import config.CAPConfig
import java.io.Serializable

class CAPSnapShot : ModelSnapshot() {
    // 配置
    var config = CAPConfig()
    // 入参数据
    var data = mutableMapOf<Int, List<Serializable>>()
}