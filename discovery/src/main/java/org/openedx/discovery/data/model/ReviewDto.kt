package org.openedx.discovery.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.discovery.domain.model.Review

data class ReviewDto(
    @SerializedName("name") val name: String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    @SerializedName("comment") val comment: String?,
    @SerializedName("designation") val designation: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("submitted_at") val submittedAt: String?,
) {
    fun mapToDomain() = Review(
        name = name.orEmpty(),
        profilePicture = profilePicture.orEmpty(),
        comment = comment.orEmpty(),
        designation = designation.orEmpty(),
        rating = (rating ?: 0.0).toDouble(),
        submittedAt = submittedAt.orEmpty()
    )
}
