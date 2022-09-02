package net.reincarnatey

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.message.data.content
import net.reincarnatey.Util.buildRegex
import net.reincarnatey.Util.buildTips

class DisableRequestJob(private val user: Friend, private val botId: Long) : CompletableJob by SupervisorJob(){

    suspend fun startDisable() {
        user.sendMessage(Config.disableInput.replace("{allTriggers}", buildTips(Config.allTriggers)))
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@DisableRequestJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }

            channel.subscribeFriendMessages {
                (sentBy(user)) {
                    if (message.content.matches(buildRegex(Config.allTriggers))){
                        Data.data[botId]?.get(user.id)?.forEach { (_, request) ->
                            request.enabled = false
                        }
                    } else {
                        Data.data[botId]?.get(user.id)?.get(message.content)?.enabled = false
                    }
                    user.sendMessage(Config.disableMessage.replace("{name}", message.content))
                    this@DisableRequestJob.cancel()
                }
            }
        }
    }
}