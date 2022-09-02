package net.reincarnatey

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.info
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.math.max

class RequestTask : TimerTask() {

    override fun run() = runBlocking {
        TimerRequestor.logger.info { "开始进行请求！" }
        val start = Date().time
        val client = OkHttpClient()
        Bot.instances.forEach { bot ->
            Data.data[bot.id]?.forEach { (user, userRequests) ->
                val userInstance = bot.getFriend(user)
                userRequests.forEach { (name, ur) ->
                    if (ur.enabled) {
                        val requestBuilder = Request.Builder()
                            .url(ur.url)
                            .method("GET", null)
                        ur.headers.forEach { (key, value) ->
                            requestBuilder.addHeader(key, value)
                        }
                        client.newCall(requestBuilder.build())
                            .enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    if(Config.sendMessage){
                                        TimerRequestor.launch {
                                            userInstance?.sendMessage(Config.errorMessage.replace("{name}", name).replace("{code}", e.localizedMessage))
                                        }
                                    }
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    if(Config.sendMessage){
                                        TimerRequestor.launch {
                                            if (response.code == 200){
                                                userInstance?.sendMessage(Config.successMessage.replace("{name}", name))
                                            } else {
                                                userInstance?.sendMessage(Config.errorMessage.replace("{name}", name).replace("{code}", response.code.toString()))
                                            }
                                            response.close()
                                        }
                                    }
                                }
                            })
                        delay(max(Config.requestDelay, 50L))
                    }
                }
            }
        }
        TimerRequestor.logger.info { "所有请求完毕！共用时${Date().time-start}！" }
    }
}