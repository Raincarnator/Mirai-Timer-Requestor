package net.reincarnatey

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.message.data.content
import net.reincarnatey.Util.buildRegex
import net.reincarnatey.Util.buildTips
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class AddRequestJob(private val user: Friend, private val botId: Long) : CompletableJob by SupervisorJob(){
    private var name = ""
    private val ur: UserRequest = UserRequest()

    suspend fun startAdd() {
        user.sendMessage(Config.addTip.replace("{cancelTriggers}", buildTips(Config.cancelTriggers)))
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@AddRequestJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }

            channel.subscribeFriendMessages(priority = EventPriority.HIGH) {
                (matching(buildRegex(Config.addTriggers)) and sentBy(user)) reply {
                    this.intercept()
                    Config.inAddTip.replace("{cancelTriggers}", buildTips(Config.cancelTriggers))
                }
                (matching(buildRegex(Config.cancelTriggers)) and sentBy(user)) {
                    user.sendMessage(Config.cancelMessage)
                    this@AddRequestJob.cancel()
                }
            }
        }

        editName()
    }

    private suspend fun editName() {
        user.sendMessage(Config.nameInput)
        var finish = false
        val editNameJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(editNameJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }
        }
        val job = scopedChannel.subscribeFriendMessages {
            (sentBy(user)) {
                message.content.let {
                    if (it.isNotEmpty() && it.isNotBlank()){
                        name = it
                        finish = true
                        editNameJob.cancel()
                    } else {
                        user.sendMessage(Config.errorInputTips)
                    }
                }
            }
        }
        job.join()
        if (finish) editUrl()
    }

    private suspend fun editUrl() {
        user.sendMessage(Config.urlInput)
        var finish = false
        val editUrlJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(editUrlJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }
        }
        val job = scopedChannel.subscribeFriendMessages {
            (sentBy(user)) {
                message.content.let {
                    if (it.isNotEmpty() && it.isNotBlank() && message.content.matches(Regex("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"))){
                        ur.url = it
                        finish = true
                        editUrlJob.cancel()
                    } else {
                        user.sendMessage(Config.errorInputTips)
                    }
                }
            }
        }
        job.join()
        if (finish) freeEdit()
    }

    private suspend fun freeEdit() {
        user.sendMessage(Config.headerTip.replace("{headerTriggers}", buildTips(Config.headerTriggers)).replace("{finishTriggers}", buildTips(Config.finishTriggers)))
        var finish = false
        var editHeader = false
        val freeEditJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(freeEditJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }
        }
        val job = scopedChannel.subscribeFriendMessages {
            (matching(buildRegex(Config.headerTriggers)) and sentBy(user)) {
                editHeader = true
                freeEditJob.cancel()
            }

            (matching(buildRegex(Config.finishTriggers)) and sentBy(user)) {
                finish = true
                freeEditJob.cancel()
            }
        }
        job.join()
        if (finish) finish()
        if (editHeader) editHeaderKey()
    }

    private suspend fun editHeaderKey(){
        user.sendMessage(Config.headerKeyInput)
        var finish = false
        var key = ""
        val editHeaderJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(editHeaderJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }
        }
        val job = scopedChannel.subscribeFriendMessages {
            (sentBy(user)) {
                message.content.let {
                    if (it.isNotEmpty() && it.isNotBlank()){
                        key = it
                        finish = true
                        editHeaderJob.cancel()
                    } else {
                        user.sendMessage(Config.errorInputTips)
                    }
                }
            }
        }
        job.join()
        if (finish) editHeaderValue(key)
    }

    private suspend fun editHeaderValue(key: String){
        user.sendMessage(Config.headerValueInput)
        var finish = false
        val editHeaderJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(editHeaderJob)
                .filterIsInstance<FriendMessageEvent>()
                .filter { it.sender.id == user.id }
        }
        val job = scopedChannel.subscribeFriendMessages {
            (sentBy(user)) {
                message.content.let {
                    if (it.isNotEmpty() && it.isNotBlank()){
                        ur.headers[key] = it
                        finish = true
                        editHeaderJob.cancel()
                    } else {
                        user.sendMessage(Config.errorInputTips)
                    }
                }
            }
        }
        job.join()
        if (finish) freeEdit()
    }

    private suspend fun finish(){
        if (Data.data[botId] == null){
            Data.data[botId] = mutableMapOf()
        }
        if (Data.data[botId]!![user.id] == null){
            Data.data[botId]!![user.id] = mutableMapOf()
        }
        Data.data[botId]!![user.id]!![name] = ur
        user.sendMessage(Config.addMessage.replace("{name}", name))
        if (Config.callWhenAdd) {
            runBlocking {
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
                                Bot.getInstance(botId).getFriend(user.id)?.sendMessage(Config.errorMessage.replace("{name}", name).replace("{code}", e.localizedMessage))
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            TimerRequestor.launch {
                                if (response.code == 200){
                                    Bot.getInstance(botId).getFriend(user.id)?.sendMessage(Config.successMessage.replace("{name}", name))
//                                    user.sendMessage(Config.successMessage.replace("{name}", name))
                                } else {
                                    Bot.getInstance(botId).getFriend(user.id)?.sendMessage(Config.errorMessage.replace("{name}", name).replace("{code}", response.code.toString()))
//                                    user.sendMessage(Config.errorMessage.replace("{name}", name).replace("{code}", response.code.toString()))
                                }
                                response.close()
                            }
                        }
                    })
            }
        }
        this@AddRequestJob.cancel()
    }
}