package net.reincarnatey

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.reincarnatey.Util.buildRegex

object Listener {
    fun start(plugin: KotlinPlugin){
        plugin.globalEventChannel().filterIsInstance<FriendMessageEvent>().filter { if (Config.enableWhitelist) it.sender.id in Config.whitelist else true}.subscribeFriendMessages {
            matching(buildRegex(Config.addTriggers)) {
                if (Config.requestLimit > 0 && (Data.data[bot.id]?.get(sender.id)?.size ?: 0) >= Config.requestLimit){
                    sender.sendMessage(Config.limitedMessage.replace("{limit}", Config.requestLimit.toString()))
                } else {
                    plugin.launch { AddRequestJob(friend, bot.id).startAdd() }
                }
            }

            matching(buildRegex(Config.viewTriggers)) {
                plugin.launch { ViewRequestJob(friend, bot.id).startView() }
            }

            matching(buildRegex(Config.disableTriggers)) {
                plugin.launch { DisableRequestJob(friend, bot.id).startDisable() }
            }

            matching(buildRegex(Config.enableTriggers)) {
                plugin.launch { EnableRequestJob(friend, bot.id).startEnable() }
            }

            matching(buildRegex(Config.deleteTriggers)) {
                plugin.launch { DeleteRequestJob(friend, bot.id).startDelete() }
            }
        }
    }
}