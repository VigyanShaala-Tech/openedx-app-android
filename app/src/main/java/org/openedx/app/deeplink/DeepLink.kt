package org.openedx.app.deeplink

import android.net.Uri

class DeepLink(params: Map<String, String>) {

    private val mergedParams: Map<String, String> = mergeUrlParams(params)

    private val screenName = mergedParams[Keys.SCREEN_NAME.value]
        ?: mergedParams["screen_name"]
        ?: mergedParams["screenName"]
        ?: mergedParams["screen"]
        ?: mergedParams["scr"]
    private val notificationType = mergedParams[Keys.NOTIFICATION_TYPE.value]
        ?: mergedParams["notification_type"]
        ?: mergedParams["notificationType"]
        ?: mergedParams["type"]
        ?: mergedParams["action"]
        ?: mergedParams["act"]
    val courseId = mergedParams[Keys.COURSE_ID.value]
        ?: mergedParams[Keys.COURSE_ID_ALT.value]
        ?: mergedParams["course_id"]
        ?: mergedParams["courseId"]
        ?: mergedParams["course"]
        ?: mergedParams["c_id"]
        ?: mergedParams["CId"]
    val pathId = mergedParams[Keys.PATH_ID.value] ?: mergedParams["path_id"] ?: mergedParams["pathId"]
    val componentId = mergedParams[Keys.COMPONENT_ID.value] ?: mergedParams["component_id"] ?: mergedParams["componentId"]
    val topicId = mergedParams[Keys.TOPIC_ID.value] ?: mergedParams["topic_id"] ?: mergedParams["topicId"]
    val threadId = mergedParams[Keys.THREAD_ID.value] ?: mergedParams["thread_id"] ?: mergedParams["threadId"]
    val commentId = mergedParams[Keys.COMMENT_ID.value] ?: mergedParams["comment_id"] ?: mergedParams["commentId"]
    val parentId = mergedParams[Keys.PARENT_ID.value] ?: mergedParams["parent_id"] ?: mergedParams["parentId"]
    val token = mergedParams[Keys.TOKEN.value] ?: mergedParams["token"]
    val activationId = mergedParams[Keys.ACTIVATION_ID.value] ?: mergedParams["activationId"] ?: mergedParams["activation_id"]
    val meetingId = mergedParams[Keys.MEETING_ID.value] ?: mergedParams["meetingId"] ?: mergedParams["meeting_id"] ?: mergedParams["mId"]

    val type: DeepLinkType = determineType(screenName, notificationType, mergedParams)

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
        val typeParam = params["type"] ?: params["screen"] ?: params["action"] ?: params["notification_type"] ?: params["scr"] ?: params["act"]
        if (!typeParam.isNullOrBlank()) {
            val resolved = DeepLinkType.typeOf(typeParam)
            if (resolved != DeepLinkType.NONE) return resolved
        }
        if (!meetingId.isNullOrBlank()) {
            return DeepLinkType.MEETING
        }
        if (!courseId.isNullOrBlank()) {
            return DeepLinkType.COURSE_DASHBOARD
        }
        return DeepLinkType.NONE
    }

    companion object {
        fun mergeUrlParams(params: Map<String, String>): Map<String, String> {
            val map = params.toMutableMap()
            val urlCandidate = map["url"]
                ?: map["link"]
                ?: map["deeplink"]
                ?: map["deep_link"]
                ?: map["target_url"]
                ?: map["uri"]
                ?: map.values.firstOrNull { it.startsWith("http://") || it.startsWith("https://") }

            if (!urlCandidate.isNullOrBlank()) {
                try {
                    val cleanUrl = urlCandidate.replace("&&", "&")
                    val uri = Uri.parse(cleanUrl)
                    uri.queryParameterNames.forEach { name ->
                        uri.getQueryParameter(name)?.let { value ->
                            if (!map.containsKey(name)) {
                                map[name] = if (name == "CId" || name == "course_id") {
                                    value.replace(" ", "+")
                                } else {
                                    value
                                }
                            }
                        }
                    }
                    val pathSegments = uri.pathSegments
                    if (pathSegments.isNotEmpty()) {
                        for (i in pathSegments.indices) {
                            val segment = pathSegments[i]
                            if ((segment == "course" || segment == "courses") && i + 1 < pathSegments.size) {
                                if (!map.containsKey("course_id") && !map.containsKey("CId")) {
                                    map["course_id"] = pathSegments[i + 1].replace(" ", "+")
                                }
                            }
                            if ((segment == "join" || segment == "meeting" || segment == "live-session") && i + 1 < pathSegments.size) {
                                val possibleMeetingId = pathSegments[i + 1]
                                if (possibleMeetingId != "join" && possibleMeetingId != "meeting") {
                                    if (!map.containsKey("mId") && !map.containsKey("meetingId")) {
                                        map["mId"] = possibleMeetingId
                                    }
                                }
                                if (!map.containsKey("screen_name") && !map.containsKey("scr")) {
                                    map["screen_name"] = "meeting"
                                }
                            }
                            if (segment == "live-session" || segment == "meeting") {
                                if (!map.containsKey("screen_name") && !map.containsKey("scr")) {
                                    map["screen_name"] = "meeting"
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return map
        }
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
