package cn.chahuyun.config

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class RegexConst(
    @SerialName("order")
    val order: String,

    @SerialName("premission")
    val premission: String,

    @SerialName("const")
    val const: Double,
)
