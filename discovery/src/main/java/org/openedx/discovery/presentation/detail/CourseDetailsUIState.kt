package org.openedx.discovery.presentation.detail

import org.openedx.discovery.domain.model.Course

sealed class CourseDetailsUIState {
    data class CourseData(
        val course: Course,
        val isUserLoggedIn: Boolean = false,
        val isWishlisted: Boolean = false,
        val curriculum: Map<String, List<String>> = emptyMap(),
        val instructors: List<org.openedx.discovery.domain.model.Instructor> = emptyList(),
        val reviews: List<org.openedx.discovery.domain.model.Review> = emptyList(),
    ) :
        CourseDetailsUIState()

    data object Loading : CourseDetailsUIState()
}
