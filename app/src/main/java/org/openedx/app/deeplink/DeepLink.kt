package org.openedx.app.deeplink

class DeepLink(params: Map<String, String>) {

    private val screenName = params[Keys.SCREEN_NAME.value]
        ?: params["screen_name"]
        ?: params["screenName"]
        ?: params["screen"]
    private val notificationType = params[Keys.NOTIFICATION_TYPE.value]
        ?: params["notification_type"]
        ?: params["notificationType"]
        ?: params["type"]
        ?: params["action"]
    val courseId = params[Keys.COURSE_ID.value]
        ?: params[Keys.COURSE_ID_ALT.value]
        ?: params["course_id"]
        ?: params["courseId"]
        ?: params["course"]
        ?: params["c_id"]
        ?: params["CId"]
    val pathId = params[Keys.PATH_ID.value] ?: params["path_id"] ?: params["pathId"]
    val componentId = params[Keys.COMPONENT_ID.value] ?: params["component_id"] ?: params["componentId"]
    val topicId = params[Keys.TOPIC_ID.value] ?: params["topic_id"] ?: params["topicId"]
    val threadId = params[Keys.THREAD_ID.value] ?: params["thread_id"] ?: params["threadId"]
    val commentId = params[Keys.COMMENT_ID.value] ?: params["comment_id"] ?: params["commentId"]
    val parentId = params[Keys.PARENT_ID.value] ?: params["parent_id"] ?: params["parentId"]
    val token = params[Keys.TOKEN.value] ?: params["token"]
    val activationId = params[Keys.ACTIVATION_ID.value] ?: params["activationId"] ?: params["activation_id"]
    val meetingId = params[Keys.MEETING_ID.value] ?: params["meetingId"] ?: params["meeting_id"]

    val type: DeepLinkType = determineType(screenName, notificationType, params)

    private fun determineType(
        screenName: String?,
        notificationType: String?,
        params: Map<String, String>
    ): DeepLinkType {
        if (!screenName.isNullOrBlank()) {
            val resolved = DeepLinkType.typeOf(screenName)
            if (resolved != DeepLinkType.NONE) return resolved
        }
        if (!notificationType.isNullOrBlank()) {
            val resolved = DeepLinkType.typeOf(notificationType)
            if (resolved != DeepLinkType.NONE) return resolved
        }
        val typeParam = params["type"] ?: params["screen"] ?: params["action"] ?: params["notification_type"]
        if (!typeParam.isNullOrBlank()) {
            val resolved = DeepLinkType.typeOf(typeParam)
            if (resolved != DeepLinkType.NONE) return resolved
        }
        if (!courseId.isNullOrBlank()) {
            return DeepLinkType.COURSE_DASHBOARD
        }
        return DeepLinkType.NONE
    }

    enum class Keys(val value: String) {
        SCREEN_NAME("screen_name"),
        NOTIFICATION_TYPE("notification_type"),
        COURSE_ID("course_id"),
        COURSE_ID_ALT("CId"),
        PATH_ID("path_id"),
        COMPONENT_ID("component_id"),
        TOPIC_ID("topic_id"),
        THREAD_ID("thread_id"),
        COMMENT_ID("comment_id"),
        PARENT_ID("parent_id"),
        TOKEN("token"),
        ACTIVATION_ID("activationId"),
        MEETING_ID("mId"),
    }
}

enum class DeepLinkType(val type: String) {
    DASHBOARD("dashboard"),
    COURSE_WARE("CourseWare"),
    MEETING("meeting"),
    SIGNUP("signup"),
    SIGNIN("signin"),
    PASSWORD_RESET("PasswordReset"),
    ACCOUNT_ACTIVATION("accActivation"),
    DISCOVERY("discovery"),
    DISCOVERY_COURSE_DETAIL("discovery_course_detail"),
    DISCOVERY_PROGRAM_DETAIL("discovery_program_detail"),
    COURSE_DASHBOARD("course_dashboard"),
    COURSE_VIDEOS("course_videos"),
    COURSE_DISCUSSION("course_discussion"),
    COURSE_DATES("course_dates"),
    COURSE_HANDOUT("course_handout"),
    COURSE_ANNOUNCEMENT("course_announcement"),
    COURSE_COMPONENT("course_component"),
    PROGRAM("program"),
    DISCUSSION_TOPIC("discussion_topic"),
    DISCUSSION_POST("discussion_post"),
    DISCUSSION_COMMENT("discussion_comment"),
    PROFILE("profile"),
    USER_PROFILE("user_profile"),
    ENROLL("enroll"),
    UNENROLL("unenroll"),
    ADD_BETA_TESTER("add_beta_tester"),
    REMOVE_BETA_TESTER("remove_beta_tester"),
    FORUM_RESPONSE("forum_response"),
    FORUM_COMMENT("forum_comment"),
    NONE("");

    companion object {
        fun typeOf(type: String): DeepLinkType {
            return entries.firstOrNull { it.type.equals(type, ignoreCase = true) } ?: NONE
        }
    }
}
