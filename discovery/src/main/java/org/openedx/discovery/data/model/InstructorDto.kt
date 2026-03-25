package org.openedx.discovery.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.discovery.domain.model.Instructor

data class InstructorDto(
    @SerializedName("name") val name: String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("designation") val designation: String?,
) {
    fun mapToDomain() = Instructor(
        name = name.orEmpty(),
        profilePicture = profilePicture.orEmpty(),
        bio = bio.orEmpty(),
        designation = designation.orEmpty()
    )
}
