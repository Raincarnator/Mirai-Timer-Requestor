package net.reincarnatey

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Data : AutoSavePluginData("RequesterData") {
    var data: MutableMap<Long, MutableMap<Long, MutableMap<String, UserRequest>>> by value()
}

@Serializable
data class UserRequest(
    var url: String = "",
    var headers: MutableMap<String, String> = mutableMapOf(),
    var enabled: Boolean = true
)