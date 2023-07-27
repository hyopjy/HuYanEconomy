package cn.chahuyun.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CarDetail(
    @SerialName("carName")
    var carName: String,
    @SerialName("carNumber")
    val carNumber: Int,
    @SerialName("carDesc")
    val carDesc: String,
    @SerialName("random")
    val random: String,
    @SerialName("carUser")
    val carUser: List<Long>,
    @SerialName("driverUser")
     var driverUser: Long
)