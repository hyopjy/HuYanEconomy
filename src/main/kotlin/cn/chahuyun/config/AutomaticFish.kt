package cn.chahuyun.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutomaticFish(
    // todo
    @SerialName("name")
    val name: String,
)
