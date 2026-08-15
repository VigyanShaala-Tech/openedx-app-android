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
        setGeolocationEnabled(true)
        saveFormData = true
        mediaPlaybackRequiresUserGesture = false
        
        @Suppress("DEPRECATION")
        allowFileAccessFromFileURLs = true
        @Suppress("DEPRECATION")
        allowUniversalAccessFromFileURLs = true
        
        // Browser-like capabilities
        databaseEnabled = true
        domStorageEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
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

        if (AppDataConstants.ZOOM_URL_PATTERNS.any { url.contains(it) }) {
            userAgentString = AppDataConstants.DESKTOP_USER_AGENT
            isSpecializedUA = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
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
