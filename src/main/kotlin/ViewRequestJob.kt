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

class ViewRequestJob(private val user: Friend, private val botId: Long) : CompletableJob by SupervisorJob(){

    suspend fun startView() {
        user.sendMessage(Config.viewInput.replace("{allTriggers}", buildTips(Config.allTriggers)))
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@ViewRequestJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }

            channel.subscribeFriendMessages {
                (sentBy(user)){
                    if (message.content.matches(buildRegex(Config.allTriggers))){
                        val sb = StringBuilder(Config.noDataTip)
                        Data.data[bot.id]?.get(user.id)?.run {
                            sb.clear()
                            sb.append("${user.nick} 共 ${this.size} 个请求\n")
                            this.forEach { (it_name, it) ->
                                sb.append("${if (it.enabled) "●" else "○"} ${it_name}\n")
                            }
                        }
                        user.sendMessage(sb.toString())
                    } else {
                        var str = Config.noDataTip
                        Data.data[botId]?.get(user.id)?.get(message.content)?.let{
                            str = "请求详细: \n名称: ${message.content}\n状态: ${if (it.enabled) "● 启用中" else "○ 禁用中"}\n请求头: 共${it.headers.size}个\n目标: ${it.url}"
                        }
                        user.sendMessage(str)
                    }
                    this@ViewRequestJob.cancel()
                }
            }
        }
    }
}