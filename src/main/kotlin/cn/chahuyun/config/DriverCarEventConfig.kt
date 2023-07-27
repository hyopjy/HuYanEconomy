package cn.chahuyun.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object DriverCarEventConfig : AutoSavePluginConfig("DriverCarEventConfig") {

    @ValueDescription("开车列表")
    var driverCar: Map<Long, List<CarDetail>> by value()
}