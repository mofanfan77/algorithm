package catalog.component

import core.entity.EntityOnResource

class JSPRuleManager: RuleManager() {
    private val allowedLinks = mutableMapOf<EntityOnResource, ArrayList<String>>()

    /**
     * 考虑禁用规则
     */
    fun addLinks(links: List<Triple<EntityOnResource, EntityOnResource, Boolean>>) {
        for (link in links){
            allowedLinks.putIfAbsent(link.first, arrayListOf())
            if (link.third){
                allowedLinks[link.first]?.add(link.second.resourceId)
            }
        }
    }

    fun getAllowedList(eor: EntityOnResource): ArrayList<String> {
        return allowedLinks[eor] ?: arrayListOf()
    }

    fun reset(){
        allowedLinks.clear()
    }
}