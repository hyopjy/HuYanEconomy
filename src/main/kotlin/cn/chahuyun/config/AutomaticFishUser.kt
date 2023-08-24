package cn.chahuyun.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutomaticFishUser(
    @SerialName("fishUser")
    var fishUser: Long,

    @SerialName("openTime")
    val openTime: String,

    @SerialName("endTime")
    val endTime: String,

    @SerialName("cron")
    val cron: String,

    @SerialName("automaticFishList")
    val automaticFishList: List<AutomaticFish>,
)
