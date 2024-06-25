package catalog.extensions.callback

import utility.exception.CallBackDuplicateException

class XCallBackHandler : CallBackHandler() {

    override fun create(name: String, func: (String) -> Unit) {
        val operator = XCallBackOperator(func, name)
        if (name in cbList){
            throw CallBackDuplicateException("出现同样的callBack Function $name")
        }
        this.cbList[name] = operator
    }
}