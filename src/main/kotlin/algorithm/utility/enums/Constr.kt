package utility.enums

enum class Constr(val value: Int, val desc: String) {
    Precedent(0, "前序工艺约束"),
    Calendar(1, "日历时间约束"),
    Previous(2, "前置工艺约束"),
    Maximize(3, "最大值约束"),
    Sum(4, "等式约束"),
    Condition(5, "逻辑约束"),
    CleanRule(6, "清场/切换约束"),
    Subsequent(7, "后序工艺约束"),
    Minus(8, "减法约束")
}

enum class RuleEnum(val value: Int, val desc: String) {
    MinClean(1, "小清场规则"),
    MaxClean(2, "大清场规则")
}

