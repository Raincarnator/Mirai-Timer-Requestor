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

class DeleteRequestJob(private val user: Friend, private val botId: Long) : CompletableJob by SupervisorJob(){

    suspend fun startDelete() {
        user.sendMessage(Config.deleteInput.replace("{allTriggers}", buildTips(Config.allTriggers)))
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@DeleteRequestJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }

            channel.subscribeFriendMessages {
                (sentBy(user)) {
                    if (message.content.matches(buildRegex(Config.allTriggers))){
                        Data.data[botId]?.remove(user.id)
                    } else {
                        Data.data[botId]?.get(user.id)?.remove(message.content)
                    }
                    user.sendMessage(Config.deleteMessage.replace("{name}", message.content))
                    this@DeleteRequestJob.cancel()
                }
            }
        }
    }
}