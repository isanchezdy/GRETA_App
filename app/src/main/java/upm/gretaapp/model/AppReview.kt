package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppReview(
    @SerialName("ID")
    val id: Long,

    @SerialName("UserID")
    val userId: Long,

    @SerialName("GlobalComments")
    val globalComments: String,

    @SerialName("GlobalScore")
    val globalScore: Double
)