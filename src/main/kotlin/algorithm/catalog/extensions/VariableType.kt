package catalog.extensions

enum class VariableType(val value: Int, val desc: String) {
    Float(1, "小数"),
    Integer(2, "整数"),
    Binary(3, "01整数")
}

enum class ObjectiveType(val value: Int, val desc: String) {
    Minimize(1, "最小化"),
    Maximize(2, "最大化")
}

enum class ConstraintType(val value: Int, val desc: String) {
    eq(1, "="),
    ge(2, ">="),
    le(3, "<=")
}