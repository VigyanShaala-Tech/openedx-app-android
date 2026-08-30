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
        domStorageEnabled = true
        databaseEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        textZoom = 100
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
        loadsImagesAutomatically = true
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(true)
        allowFileAccess = true
        allowContentAccess = true
        
        // Essential for modern React/SPA redirects and data handling
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Use a very modern Chrome User Agent to ensure React features work
        userAgentString = AppDataConstants.DESKTOP_USER_AGENT

        if (url.contains("zoom.us") || url.contains("zoom.com") || url.contains("/meeting/") || url.contains("/join/")) {
            isSpecializedUA = true
            setSupportMultipleWindows(true)
            // Use a clean, modern Chrome UA with explicit device info to unlock all Zoom features
            userAgentString = AppDataConstants.DESKTOP_USER_AGENT
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
