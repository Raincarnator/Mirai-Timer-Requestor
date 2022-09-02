package net.reincarnatey

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("RequesterConfig") {
    @ValueDescription("每日自动请求的时间，目前只能统一进行请求且一天一次，范围为0~23")
    var requestTime by value(8)

    @ValueDescription("是否启用白名单")
    var enableWhitelist by value(false)

    @ValueDescription("白名单，启用后不在白名单上不会回应")
    val whitelist by value(mutableListOf<Long>())

    @ValueDescription("是否在用户添加和启用后立即调用一次(管理员启用时不调用)并通知结果")
    var callWhenAdd by value(true)

    @ValueDescription("可添加请求数量上限，小于等于0则为无限")
    var requestLimit by value(0)

    @ValueDescription("请求间隔时间，单位为毫秒，不建议间隔太短，最低50")
    var requestDelay by value(500L)

    @ValueDescription("到达添加上限时的回应消息")
    val limitedMessage by value("哎呀，你已经达到上限 {limit} 个了，不可以继续添加了！")

//    @ValueDescription("管理员名单，可以查看、启用、停用、删除他人设定的任务")
//    val adminList by value(mutableListOf<Long>())

    @ValueDescription("在自动请求后是否会私聊通知对应用户")
    var sendMessage by value(true)

    @ValueDescription("请求状态码为200时发送的通知")
    val successMessage by value("{name} 请求已发送成功！")

    @ValueDescription("请求状态码异常时发送的通知")
    val errorMessage by value("诶呀，{name} 请求返回了 {code} 呢……")

    @ValueDescription("成功添加新请求时的回应消息")
    val addMessage by value("{name} 请求添加成功！")

    @ValueDescription("停用请求时的回应消息")
    val disableMessage by value("{name} 请求已停用！")

    @ValueDescription("启用请求时的回应消息")
    val enableMessage by value("{name} 请求已重新启用！")

    @ValueDescription("删除请求时的回应消息")
    val deleteMessage by value("{name} 请求已删除！")

    @ValueDescription("管理员停用请求时的通知")
    val adminDisableMessage by value("你的 {name} 请求已被管理员 @{adminId} 禁用！")

    @ValueDescription("管理员启用请求时的通知")
    val adminEnableMessage by value("你的 {name} 请求已被管理员 @{adminId} 重新启用！")

    @ValueDescription("管理员删除请求时的通知")
    val adminDeleteMessage by value("你的 {name} 请求已被管理员 @{adminId} 删除！")

    @ValueDescription("添加请求的命令")
    val addTriggers by value(mutableListOf("添加请求", "增加请求"))

    @ValueDescription("查看自己请求的命令")
    val viewTriggers by value(mutableListOf("查看请求", "我的请求", "查看我的请求"))

    @ValueDescription("查看自己请求的输入")
    val viewInput by value("请输入\"{allTriggers}\" 或 要查看的请求的名称:")

    @ValueDescription("查看自己请求的命令")
    val allTriggers by value(mutableListOf("全部", "全部请求", "所有"))

    @ValueDescription("查询不到的提示")
    val noDataTip by value("数据不存在？！")

    @ValueDescription("停用请求的命令")
    val disableTriggers by value(mutableListOf("停用请求", "关闭请求", "禁用请求", "停止请求"))

    @ValueDescription("查看自己请求的输入")
    val disableInput by value("请输入\"{allTriggers}\" 或 要禁用的请求的名称:")

    @ValueDescription("启用请求的命令")
    val enableTriggers by value(mutableListOf("启用请求", "开启请求"))

    @ValueDescription("查看自己请求的输入")
    val enableInput by value("请输入\"{allTriggers}\" 或 要启用的请求的名称:")

    @ValueDescription("删除请求的命令")
    val deleteTriggers by value(mutableListOf("删除请求", "移除请求"))

    @ValueDescription("查看自己请求的输入")
    val deleteInput by value("请输入\"{allTriggers}\" 或 要删除的请求的名称:")

    @ValueDescription("开始添加请求时的提示")
    val addTip by value("开始添加请求，你可以随时输入\"{cancelTriggers}\"来取消本次添加")

    @ValueDescription("重复开始添加请求时的提示")
    val inAddTip by value("已经开始添加了哦！你可以随时输入\"{cancelTriggers}\"来取消本次添加")

    @ValueDescription("添加请求时的名称输入")
    val nameInput by value("请输入名字:")

    @ValueDescription("添加请求时的非法输入的提示")
    val errorInputTips by value("非法输入，请修改并重新发送")

    @ValueDescription("添加请求时的目标输入")
    val urlInput by value("请输入(完整的带参数的)目标URL:")

    @ValueDescription("添加请求时添加请求头提示")
    val headerTip by value("添加成功！请输入\"{headerTriggers}\"继续添加请求头(如Cookie)，或是输入\"{finishTriggers}\"完成添加")

    @ValueDescription("添加请求时添加请求头输入")
    val headerTriggers by value(mutableListOf("添加", "添加请求头"))

    @ValueDescription("添加请求头时的键提示")
    val headerKeyInput by value("请输入请求头的键:")

    @ValueDescription("添加请求头时的值提示")
    val headerValueInput by value("请输入请求头的值:")

    @ValueDescription("添加请求时完成命令")
    val finishTriggers by value(mutableListOf("完成", "完成添加"))

    @ValueDescription("添加请求时取消命令")
    val cancelTriggers by value(mutableListOf("取消", "停止添加"))

    @ValueDescription("取消添加请求时的回应消息")
    val cancelMessage by value("已取消本次添加！")
}