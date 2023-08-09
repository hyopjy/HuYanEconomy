package cn.chahuyun.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.util.concurrent.ConcurrentHashMap

object DriverCarEventConfig : AutoSavePluginConfig("DriverCarEventConfig") {

    @ValueDescription("开车列表")
    @Serializable
    var driverCar: ConcurrentHashMap<Long, List<CarDetail>> =  ConcurrentHashMap();
}