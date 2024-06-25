package config

import utility.enums.ParameterEnum
import java.io.Serializable

abstract class BaseConfig : Serializable {
    private val settingList = mutableMapOf<ParameterEnum, Any>()


    open fun set(parameter: ParameterEnum, value: Any){
        settingList[parameter] = value
    }

    fun logOutSettings() : String{
        var ans = "Parameter Setting: \n"
        for ((s, setting) in settingList){
            ans += "${s.name.padStart(20)} : ${setting.toString().padEnd(8)} \n"
        }
        return ans
    }

    open fun getAsDouble(key: ParameterEnum): Double {
        return 0.0
    }

    open fun getAsInt(key: ParameterEnum): Int {
        return 0
    }

    fun getAsString(key: ParameterEnum): String {
        return ""
    }

    open fun getAsBoolean(key: ParameterEnum): Boolean {
        return true
    }


}