package net.reincarnatey

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.message.data.content
import net.reincarnatey.Util.buildRegex
import net.reincarnatey.Util.buildTips
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class EnableRequestJob(private val user: Friend, private val botId: Long) : CompletableJob by SupervisorJob(){

    suspend fun startEnable() {
        user.sendMessage(Config.enableInput.replace("{allTriggers}", buildTips(Config.allTriggers)))
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@EnableRequestJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }

            channel.subscribeFriendMessages {
                (sentBy(user)) {
                    if (message.content.matches(buildRegex(Config.allTriggers))){
                        Data.data[botId]?.get(user.id)?.forEach { (_, request) ->
                            request.enabled = true
                        }
                    } else {
                        Data.data[botId]?.get(user.id)?.get(message.content)?.enabled = true
                    }
                    user.sendMessage(Config.enableMessage.replace("{name}", message.content))
                    if (Config.callWhenAdd) {
                        runBlocking {
                            Data.data[botId]?.get(user.id)?.get(message.content)?.let { ur ->
                                val requestBuilder = Request.Builder()
                                    .url(ur.url)
                                    .method("GET", null)
                                ur.headers.forEach { (key, value) ->
                                    requestBuilder.addHeader(key, value)
                                }
                                OkHttpClient.Builder()
                                    .callTimeout(5, TimeUnit.SECONDS)
                                    .connectTimeout(5, TimeUnit.SECONDS)
                                    .readTimeout(5, TimeUnit.SECONDS)
                                    .build()
                                    .newCall(requestBuilder.build())
                                    .enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            TimerRequestor.launch {
                                                Bot.getInstance(botId).getFriend(user.id)?.sendMessage(Config.errorMessage.replace("{name}", message.content).replace("{code}", e.localizedMessage))
//                                                user.sendMessage(Config.errorMessage.replace("{name}", message.content).replace("{code}", e.localizedMessage))
                                            }
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            TimerRequestor.launch {
                                                if (response.code == 200){
                                                    Bot.getInstance(botId).getFriend(user.id)?.sendMessage(Config.successMessage.replace("{name}", message.content))
//                                                    user.sendMessage(Config.successMessage.replace("{name}", message.content))
                                                } else {
                                                    Bot.getInstance(botId).getFriend(user.id)?.sendMessage(Config.errorMessage.replace("{name}", message.content).replace("{code}", response.code.toString()))
//                                                    user.sendMessage(Config.errorMessage.replace("{name}", message.content).replace("{code}", response.code.toString()))
                                                }
                                                response.close()
                                            }
                                        }
                                    })
                            }
                        }
                    }
                    this@EnableRequestJob.cancel()
                }
            }
        }
    }
}