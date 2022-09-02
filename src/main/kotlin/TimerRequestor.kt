package net.reincarnatey

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import java.time.ZoneId
import java.util.*

object TimerRequestor : KotlinPlugin(
    JvmPluginDescription(
        id = "net.reincarnatey.timer-requester",
        name = "定时请求器",
        version = "1.0",
    ) {
        author("Mitr-yuzr")
        info("""可以定时按照设定发送网络请求并私聊进行提醒，可用于自动签到、打卡等。""")
    }
) {
    private var scheduler = Timer()
    private var requestTask = RequestTask()

    override fun onEnable() {
        Config.reload()
        Data.reload()
        CommandManager.registerCommand(Command)

        initTask(Config.requestTime)
        Listener.start(this)

        logger.info { "TimerRequester准备就绪!" }
    }

    override fun onDisable() {
        requestTask.cancel()
        scheduler.cancel()
        CommandManager.unregisterCommand(Command)

        logger.info { "TimerRequester已卸载!" }
    }

    private fun initTask(time: Int){
        val date = Date.from(
            Date().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
        )
        date.time += time.run {if(this>24) 8 else this} * 60 * 60 * 1000

        if (date.before(Date())) {
            date.time += 24 * 60 * 60 * 1000
            scheduler.schedule(requestTask, date, 24 * 60 * 60 * 1000)
        } else {
            scheduler.schedule(requestTask, date, 24 * 60 * 60 * 1000)
        }
    }

    fun restartTask(){
        requestTask.cancel()
        scheduler.cancel()
        requestTask = RequestTask()
        scheduler = Timer()

        initTask(Config.requestTime)
    }
}