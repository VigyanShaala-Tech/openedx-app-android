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
        useWideViewPort = true
        textZoom = 100
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
        loadsImagesAutomatically = true
        domStorageEnabled = true
        databaseEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
        allowFileAccess = true
        allowContentAccess = true
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        // Apply a modern Mobile Chrome User Agent for ALL URLs by default
        // This makes sites treat the WebView as a full Chrome browser
        userAgentString = AppDataConstants.MOBILE_CHROME_USER_AGENT

        if (url.contains("zoom.us") || url.contains("zoom.com") || url.contains("/meeting/") || url.contains("/join/")) {
            isSpecializedUA = true
            setSupportMultipleWindows(true)
            // Use a clean, modern Chrome UA with explicit device info to unlock all Zoom features
            userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Mobile Safari/537.36"
        }
 else if (url.contains("google-calendar")) {
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
