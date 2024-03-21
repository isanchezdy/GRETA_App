package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureReview(
    @SerialName("FeatureID")
    val featureId: Long? = null,

    @SerialName("AppReviewID")
    val appReviewId: Long,

    @SerialName("Topic")
    val topic: String,

    @SerialName("Score")
    val score: Double,

    @SerialName("Comments")
    val comments: String
)