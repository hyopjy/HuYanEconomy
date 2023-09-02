package cn.chahuyun.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import java.util.concurrent.ConcurrentHashMap

object FishSignPluginConfig : AutoSavePluginConfig("FishSignPluginConfig") {
    @ValueDescription("用户兑换徽章信息")
    @Serializable
    var fishSignMap: ConcurrentHashMap<String, Set<Long>> =  ConcurrentHashMap();
}