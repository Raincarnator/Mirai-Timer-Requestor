package net.reincarnatey

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.isConsole
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit

object Command: CompositeCommand(
    TimerRequestor, "timerRequester", "tr", description = "timerRequester管理命令"
) {
    @SubCommand("addAdmin", "添加管理员")
    suspend fun addAdmin(sender: CommandSender, user: Long) {
        if (sender.isConsole()){
            AbstractPermitteeId.ExactUser(user).permit(Command.permission)
            sender.sendMessage("管理员添加成功")
        } else {
            sender.sendMessage("该命令仅限控制台使用")
        }
    }

    @SubCommand("removeAdmin", "移除管理员")
    suspend fun removeAdmin(sender: CommandSender, user: Long) {
        if (sender.isConsole()){
            AbstractPermitteeId.ExactUser(user).cancel(Command.permission, true)
            sender.sendMessage("管理员移除成功")
        } else {
            sender.sendMessage("该命令仅限控制台使用")
        }
    }

    @SubCommand("whitelist", "wl", "白名单")
    suspend fun whitelist(sender: CommandSender, enable: Boolean?) {
        if (enable != null){
            Config.enableWhitelist = enable
            sender.sendMessage("白名单已切换至 $enable")
            if (enable && sender.isUser()){
                addWhitelist(sender, sender.user.id)
            }
        } else {
            sender.sendMessage("白名单: ${Config.whitelist}")
        }
    }

    @SubCommand("addWhitelist", "addWl", "添加白名单")
    suspend fun addWhitelist(sender: CommandSender, user: Long) {
        Config.whitelist.add(user)
        sender.sendMessage("已将 $user 添加至白名单")
    }

    @SubCommand("requestTime", "time", "请求时间")
    suspend fun requestTime(sender: CommandSender, time: Int) {
        if (time in (0..23)){
            Config.requestTime = time
            TimerRequestor.restartTask()
            sender.sendMessage("已将requestTime修改为 $time")
        } else {
            sender.sendMessage("非法输入！请求时间为0-23的整数")
        }
    }

    @SubCommand("callWhenAdd")
    suspend fun callWhenAdd(sender: CommandSender, enable: Boolean) {
        Config.callWhenAdd = enable
        sender.sendMessage("已将callWhenAdd修改为 $enable")
    }

    @SubCommand("sendMessage")
    suspend fun sendMessage(sender: CommandSender, enable: Boolean) {
        Config.sendMessage = enable
        sender.sendMessage("已将sendMessage修改为 $enable")
    }

    @SubCommand("requestLimit")
    suspend fun requestLimit(sender: CommandSender, limit: Int) {
        if (limit >= 0){
            sender.sendMessage("已将requestLimit修改为 $limit")
        } else {
            sender.sendMessage("非法输入！请求数量限制应大于0")
        }
        Config.requestLimit = limit
    }

    @SubCommand("requestDelay")
    suspend fun requestDelay(sender: CommandSender, delay: Long) {
        if (delay >= 50){
            sender.sendMessage("已将requestDelay修改为 $delay")
        } else {
            sender.sendMessage("非法输入！间隔应大于50ms")
        }
        Config.requestDelay = delay
    }

    @SubCommand("list", "show", "view", "列表", "显示", "展示")
    suspend fun list(sender: CommandSender, user: Long? = null, name: String? = null) {
        val sb = StringBuilder()
        if (user == null){
            if (sender.bot == null){
                var usersCnt = 0
                var requestsCnt = 0
                Data.data.forEach{ (botId, users) ->
                    var requestsCnt2 = 0
                    usersCnt += users.size
                    users.forEach { (user, requests) ->
                        requestsCnt += requests.size
                        requestsCnt2 += requests.size
                        sb.insert(0, "$user: 共 ${requests.size} 个请求\n")
                    }
                    sb.insert(0, "[$botId]: 共 ${users.size} 个用户 $requestsCnt2 个请求\n")
                }
                sb.insert(0, "***共 ${Data.data.size} 个Bot $usersCnt 个用户 $requestsCnt 个请求***\n")
            } else {
                var requestsCnt = 0
                Data.data[sender.bot!!.id]?.forEach { (user, requests) ->
                    requestsCnt += requests.size
                    sb.insert(0, "$user: 共 ${requests.size} 个请求\n")
                }
                sb.insert(0, "[${sender.bot!!.id}]: 共 ${Data.data[sender.bot!!.id]?.size?:0} 个用户 $requestsCnt 个请求\n")
            }
        } else {
            if (sender.bot == null){
                sb.append("控制台不支持使用该命令，请使用/tr addAdmin添加管理员，并使用管理员账号私聊该Bot使用命令。")
            } else {
                if (name == null){
                    sb.append("$user 共 ${Data.data[sender.bot!!.id]?.get(user)?.size?:0} 个请求\n")
                    Data.data[sender.bot!!.id]?.get(user)?.forEach { (it_name, it) ->
                        sb.append("${if (it.enabled) "●" else "○"} ${it_name}\n")
                    }
                } else {
                    var str = Config.noDataTip
                    Data.data[sender.bot!!.id]?.get(user)?.get(name)?.let{
                        str = "请求详细: \n名称: $name\n状态: ${if (it.enabled) "● 启用中" else "○ 禁用中"}\n请求头: 共${it.headers.size}个\n目标: ${it.url}"
                    }
                    sb.append(str)
                }
            }
        }
        sender.sendMessage(sb.toString())
    }

    @SubCommand("delete", "remove", "删除", "移除")
    suspend fun remove(sender: CommandSender, user: Long, name: String? = null, sendMessage: Boolean = true) {
        if (sender.bot == null){
            sender.sendMessage("控制台不支持使用该命令，请使用/tr addAdmin添加管理员，并使用管理员账号私聊该Bot使用命令。")
            return
        }
        var target = "全部"
        if (name == null){
            Data.data[sender.bot!!.id]?.remove(user)
        } else {
            Data.data[sender.bot!!.id]?.get(user)?.remove(name)
            target = name
        }
        sender.sendMessage("已删除 $user 的 $target 请求！")
        if (sendMessage){
            sender.bot!!.getFriend(user)?.sendMessage(Config.adminDeleteMessage.replace("{name}", target).replace("{adminId}", sender.name))
        }
    }

    @SubCommand("disable", "stop", "停用", "禁用", "停止", "关闭")
    suspend fun disable(sender: CommandSender, user: Long, name: String? = null, sendMessage: Boolean = true) {
        if (sender.bot == null){
            sender.sendMessage("控制台不支持使用该命令，请使用/tr addAdmin添加管理员，并使用管理员账号私聊该Bot使用命令。")
            return
        }
        var target = "全部"
        if (name == null){
            Data.data[sender.bot!!.id]?.get(user)?.forEach { (_, request) ->
                request.enabled = false
            }
        } else {
            Data.data[sender.bot!!.id]?.get(user)?.get(name)?.enabled = false
            target = name
        }
        sender.sendMessage("已禁用 $user 的 $target 请求！")
        if (sendMessage){
            sender.bot!!.getFriend(user)?.sendMessage(Config.adminDisableMessage.replace("{name}", target).replace("{adminId}", sender.name))
        }
    }

    @SubCommand("enable", "start", "启用", "开启")
    suspend fun enable(sender: CommandSender, user: Long, name: String? = null, sendMessage: Boolean = true) {
        if (sender.bot == null){
            sender.sendMessage("控制台不支持使用该命令，请使用/tr addAdmin添加管理员，并使用管理员账号私聊该Bot使用命令。")
            return
        }
        var target = "全部"
        if (name == null){
            Data.data[sender.bot!!.id]?.get(user)?.forEach { (_, request) ->
                request.enabled = true
            }
        } else {
            Data.data[sender.bot!!.id]?.get(user)?.get(name)?.enabled = true
            target = name
        }
        sender.sendMessage("已启用 $user 的 $target 请求！")
        if (sendMessage){
            sender.bot!!.getFriend(user)?.sendMessage(Config.adminEnableMessage.replace("{name}", target).replace("{adminId}", sender.name))
        }
    }
}