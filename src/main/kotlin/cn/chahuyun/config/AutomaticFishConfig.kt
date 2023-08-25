package cn.chahuyun.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import java.util.concurrent.ConcurrentHashMap

/**
 * 钓鱼机设置
 */
object AutomaticFishConfig : AutoSavePluginConfig("AutomaticFishConfig")  {
    @ValueDescription("钓鱼机使用人列表")
    @Serializable
    var automaticFishUserMap: ConcurrentHashMap<String, AutomaticFishUser> =  ConcurrentHashMap();

}
