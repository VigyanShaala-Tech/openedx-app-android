package org.openedx.auth.presentation

interface AuthAnalytics {
    fun setUserIdForSession(userId: Long)
    fun logEvent(event: String, params: Map<String, Any?>)
    fun logScreenEvent(screenName: String, params: Map<String, Any?>)
    fun discoveryCourseClickedEvent(courseId: String, courseName: String)
}

enum class AuthAnalyticsEvent(val eventName: String, val biValue: String) {
    Logistration(
        "Logistration",
        "edx.bi.app.logistration"
    ),
    DISCOVERY_COURSES_SEARCH(
        "Logistration_Courses_Search",
        "edx.bi.app.logistration.courses_search"
    ),
    EXPLORE_ALL_COURSES(
        "Logistration_Explore_All_Courses",
        "edx.bi.app.logistration.explore.all.courses"
    ),
    SIGN_IN(
        "Logistration_Log_in",
        "edx.bi.app.logistration.signin"
    ),
    REGISTER(
        "Logistration_Register",
        "edx.bi.app.logistration.register"
    ),
    REGISTER_CLICKED(
        "Logistration_Register_Clicked",
        "edx.bi.app.logistration.register.clicked"
    ),
    CREATE_ACCOUNT_CLICKED(
        "Logistration_Create_Account_Clicked",
        "edx.bi.app.logistration.user.create_account.clicked"
    ),
    REGISTER_SUCCESS(
        "Logistration_Register_Success",
        "edx.bi.app.user.register.success"
    ),
    SIGN_IN_CLICKED(
        "Logistration_Log_in_Clicked",
        "edx.bi.app.logistration.signin.clicked"
    ),
    USER_SIGN_IN_CLICKED(
        "Logistration_User_Log_in_Clicked",
        "edx.bi.app.logistration.user.signin.clicked"
    ),
    SIGN_IN_SUCCESS(
        "Logistration_Log_in_Success",
        "edx.bi.app.user.signin.success"
    ),
    FORGOT_PASSWORD_CLICKED(
        "Logistration_Forgot_Password_Clicked",
        "edx.bi.app.logistration.forgot_password.clicked"
    ),
    RESET_PASSWORD_CLICKED(
        "Logistration_Reset_Password_Clicked",
        "edx.bi.app.user.reset_password.clicked"
    ),
    RESET_PASSWORD_SUCCESS(
        "Logistration_Reset_Password_Success",
        "edx.bi.app.user.reset_password.success"
    ),
}

enum class AuthAnalyticsKey(val key: String) {
    NAME("name"),
    SEARCH_QUERY("search_query"),
    SUCCESS("success"),
    METHOD("method"),
}
