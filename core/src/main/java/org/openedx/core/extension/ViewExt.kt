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
        builtInZoomControls = true // Standard Chrome behavior
        displayZoomControls = false // Hide the +/- buttons, use pinch-to-zoom
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
        
        // Critical: Prevent system font size scaling from breaking web layouts
        textZoom = 100
        
        // Use default caching for better performance and session persistence
        cacheMode = WebSettings.LOAD_DEFAULT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Apply a modern Mobile Chrome User Agent for ALL URLs by default
        // This makes sites treat the WebView as a full Chrome browser
        userAgentString = AppDataConstants.MOBILE_CHROME_USER_AGENT

        if (url.contains("zoom.us") || url.contains("/meeting/") || url.contains("/join/")) {
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
