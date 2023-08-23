package cn.chahuyun.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpecialTitle(
    @SerialName("qq")
    var qq: Long,

    @SerialName("title")
    val title: String,

    @SerialName("expire")
    val expire: String,
)
