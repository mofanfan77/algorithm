package core.entity

class SubVariableSequence {

    constructor()
    constructor(name: String, group: String) {
        this.id = name
        this.groupId = group
    }

    var taskSequence = ArrayList<Variable>()
    var id = ""
    var groupId = ""

    fun connect(variable: Variable) {
        val lastTask = taskSequence.lastOrNull()
        variable.addToSub(this, lastTask)
        taskSequence.add(variable)
    }
}