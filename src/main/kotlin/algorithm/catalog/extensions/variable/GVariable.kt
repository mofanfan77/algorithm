package catalog.extensions.variable

import jscip.Variable


class GVariable : IVariable {
    lateinit var core: Variable

    constructor()
    constructor(core: Variable) {
        this.core = core
    }

    override fun getGCore(): Variable {
        return core
    }
}

