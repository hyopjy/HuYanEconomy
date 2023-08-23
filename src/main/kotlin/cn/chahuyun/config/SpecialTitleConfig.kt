package cn.chahuyun.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.util.concurrent.ConcurrentHashMap


object SpecialTitleConfig : AutoSavePluginConfig("SpecialTitleConfig"){
    @ValueDescription("特殊标记")
    @Serializable
    var specialTitleMap: ConcurrentHashMap<Long, List<SpecialTitle>> =  ConcurrentHashMap();

}
