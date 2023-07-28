package cn.chahuyun.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object EconomyEventConfig : AutoSavePluginConfig("EconomyGoalConfig") {

    @ValueDescription("启动经济命令拦截群列表\n")
    var economyCheckGroup: List<Long> by value()

    @ValueDescription("随机获取WDIT的管理员列表\n")
    var economyLongByRandomAdmin: List<Long> by value()

    @ValueDescription("经济命令拦截及耗费金钱\n")
    var regexOrderCheck: List<RegexConst>  by value()

    @ValueDescription("\n")
    var regexCheck: List<RegexConst> by value()

}