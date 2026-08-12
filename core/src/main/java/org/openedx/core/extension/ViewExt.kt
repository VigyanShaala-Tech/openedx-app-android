package org.openedx.core.extension

import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openedx.core.AppDataConstants
import org.openedx.core.system.AppCookieManager

fun WebView.applyFullAccessSettings(url: String): Boolean {
    var isSpecializedUA = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    }
    with(settings) {
        javaScriptEnabled = true
        loadWithOverviewMode = true
        builtInZoomControls = false
        setSupportZoom(true)
        loadsImagesAutomatically = true
        domStorageEnabled = true
        allowFileAccess = true
        allowContentAccess = true
        useWideViewPort = true
        databaseEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(true)
        setGeolocationEnabled(true)
        saveFormData = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        if (url.contains("zoom.us") || url.contains("/meeting/") || url.contains("/join/")) {
            userAgentString = AppDataConstants.MOBILE_CHROME_USER_AGENT
            isSpecializedUA = true
            setSupportMultipleWindows(false) // Zoom 'More' button needs single window behavior in some environments
        } else if (url.contains("google-calendar")) {
            userAgentString = AppDataConstants.DESKTOP_USER_AGENT
            isSpecializedUA = true
        }
    }
    return isSpecializedUA
}

fun WebView.loadUrl(url: String, scope: CoroutineScope, cookieManager: AppCookieManager) {
    val mobileUrl = url.addMobileQueryParam()
    if (cookieManager.isSessionCookieMissingOrExpired()) {
        scope.launch {
            cookieManager.tryToRefreshSessionCookie()
            loadUrl(mobileUrl)
        }
    } else {
        loadUrl(mobileUrl)
    }
}
