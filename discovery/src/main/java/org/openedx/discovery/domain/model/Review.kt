package org.openedx.discovery.domain.model

data class Review(
    val name: String,
    val profilePicture: String,
    val comment: String,
    val designation: String,
    val rating: Int,
    val submittedAt: String,
)
