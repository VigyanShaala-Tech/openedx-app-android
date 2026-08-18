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
    settings.apply {
        javaScriptEnabled = true
        loadWithOverviewMode = true
        builtInZoomControls = true
        displayZoomControls = false
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
        
        @Suppress("DEPRECATION")
        allowFileAccessFromFileURLs = true
        @Suppress("DEPRECATION")
        allowUniversalAccessFromFileURLs = true
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mediaPlaybackRequiresUserGesture = false
        }
        
        textZoom = 100
        cacheMode = WebSettings.LOAD_DEFAULT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        userAgentString = AppDataConstants.MOBILE_CHROME_USER_AGENT

        if (AppDataConstants.ZOOM_URL_PATTERNS.any { url.contains(it) }) {
            userAgentString = AppDataConstants.DESKTOP_USER_AGENT
            isSpecializedUA = true
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
