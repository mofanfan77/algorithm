package catalog.extensions.callback

abstract class CallBackHandler {
    var cbList : MutableMap<String, CallBackOperator> = mutableMapOf()

    abstract fun create(name:String, func: (String) -> Unit )

    fun get(name:String):CallBackOperator?{
        return cbList[name]
    }
}