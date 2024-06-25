package utility.enums

import java.io.Serializable

enum class AttrClassEnum(val value: Int, val desc: String) : Serializable{
    //    IntVar(0, "整数"),
    DoubleVar(1, "浮点"),
    //    TimeVar(2, "时间"),
//    DurVar(3, "时间间隔"),
    BinVar(4, "布尔"),
    StrVar(5, "字符串"),
    EntVar(6, "对象")
}

enum class AttrTypeEnum(val value: Int, val desc: String)  : Serializable{
    Constant(0, "常数变量 —— 来自模型参数"),
    Calculation(1, "计算变量 —— 来自约束计算"),
    Decision(2, "决策变量 —— 来自约束计算"),
}