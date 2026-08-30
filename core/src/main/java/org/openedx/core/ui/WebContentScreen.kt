package org.openedx.core.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import org.openedx.core.ui.theme.appColors
import org.openedx.core.AppDataConstants
import org.openedx.core.extension.addMobileQueryParam
import org.openedx.core.extension.applyFullAccessSettings
import org.openedx.core.extension.loadUrl
import org.openedx.foundation.extension.applyDarkModeIfEnabled
import org.openedx.foundation.extension.isEmailValid
import org.openedx.foundation.extension.replaceLinkTags
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WebContentScreen(
    windowSize: WindowSize,
    apiHostUrl: String? = null,
    cookieManager: org.openedx.core.system.AppCookieManager? = null,
    title: String?,
    onBackClick: () -> Unit,
    htmlBody: String? = null,
    contentUrl: String? = null,
    canShowBackBtn: Boolean = true,
) {
    val scaffoldState = rememberScaffoldState()
    val isMeetingUrl = contentUrl?.let {
        AppDataConstants.ZOOM_URL_PATTERNS.any { pattern ->
            it.contains(pattern)
        }
    } ?: false
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth by remember(key1 = windowSize, key2 = isMeetingUrl, key3 = isLandscape) {
        mutableStateOf(
            if (isMeetingUrl && isLandscape) {
                Modifier.fillMaxWidth()
            } else {
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            }
        )
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isMeetingUrl && isLandscape) Modifier else if (isMeetingUrl) Modifier.navigationBarsInset() else Modifier.padding(bottom = 24.dp))
            .semantics {
                testTagsAsResourceId = true
            },
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .then(if (isMeetingUrl && isLandscape) Modifier else Modifier.statusBarsInset())
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(screenWidth) {
                if (((title?.isNotEmpty() == true) || canShowBackBtn) && !(isMeetingUrl && isLandscape)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .zIndex(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Toolbar(
                            label = title ?: "",
                            canShowBackBtn = canShowBackBtn,
                            onBackClick = onBackClick
                        )
                    }
                }
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.appColors.background
                ) {
                    if (htmlBody.isNullOrEmpty() && contentUrl.isNullOrEmpty()) {
                        CircularProgress()
                    } else {
                        var webViewAlpha by rememberSaveable { mutableFloatStateOf(0f) }
                        Surface(
                            Modifier.alpha(webViewAlpha),
                            color = MaterialTheme.appColors.background
                        ) {
                            WebViewContent(
                                apiHostUrl = apiHostUrl,
                                cookieManager = cookieManager,
                                body = htmlBody,
                                contentUrl = contentUrl,
                                onWebPageLoaded = {
                                    webViewAlpha = 1f
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@SuppressLint("SetJavaScriptEnabled")
private fun WebViewContent(
    apiHostUrl: String? = null,
    cookieManager: org.openedx.core.system.AppCookieManager? = null,
    body: String? = null,
    contentUrl: String? = null,
    onWebPageLoaded: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val webView = remember {
        WebView(context).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
            webViewClient = object : WebViewClient() {
                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    onWebPageLoaded()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val clickUrl = request?.url?.toString() ?: ""
                    if (clickUrl.isEmpty()) return false

                    // Don't override Zoom URLs or internal app URLs, let them load in the WebView
                    val isZoomUrl = AppDataConstants.ZOOM_URL_PATTERNS.any { clickUrl.contains(it) }
                    val isInternalUrl = apiHostUrl?.let { clickUrl.startsWith(it) } ?: false
                    
                    if (isZoomUrl || isInternalUrl || clickUrl.contains("vigyanshaala.com")) {
                        return false
                    }

                    return if (clickUrl.startsWith("http")) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl)))
                            true
                        } catch (e: Exception) {
                            false
                        }
                    } else if (clickUrl.startsWith("mailto:")) {
                        val email = clickUrl.replace("mailto:", "")
                        if (email.isEmailValid()) {
                            org.openedx.core.utils.EmailUtil.sendEmailIntent(context, email, "", "")
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    val resources = request?.resources ?: arrayOf()
                    request?.grant(resources)
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: android.webkit.GeolocationPermissions.Callback?
                ) {
                    callback?.invoke(origin, true, false)
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: android.os.Message?
                ): Boolean {
                    // Force popups to open in the same window for React consistency
                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                    transport?.webView = view
                    resultMsg?.sendToTarget()
                    return true
                }

                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    androidx.appcompat.app.AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton(org.openedx.core.R.string.core_ok) { _, _ -> result?.confirm() }
                        .setCancelable(false)
                        .create()
                        .show()
                    return true
                }

                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    androidx.appcompat.app.AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton(org.openedx.core.R.string.core_ok) { _, _ -> result?.confirm() }
                        .setNegativeButton(org.openedx.core.R.string.core_cancel) { _, _ -> result?.cancel() }
                        .setCancelable(false)
                        .create()
                        .show()
                    return true
                }

                override fun onCloseWindow(window: WebView?) {
                    super.onCloseWindow(window)
                    (context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                }

                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line " +
                            "${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    return true
                }
            }
            applyFullAccessSettings(contentUrl ?: "")
            settings.mediaPlaybackRequiresUserGesture = false
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            body?.let {
                loadDataWithBaseURL(
                    apiHostUrl,
                    body.replaceLinkTags(isDarkTheme),
                    "text/html",
                    StandardCharsets.UTF_8.name(),
                    null
                )
            }
            contentUrl?.let { url ->
                if (cookieManager != null) {
                    loadUrl(url, coroutineScope, cookieManager)
                } else {
                    loadUrl(url.addMobileQueryParam())
                }
            }
            applyDarkModeIfEnabled(isDarkTheme)
        }
    }

    AndroidView(
        factory = {
            webView
        },
        update = { webView ->
            body?.let {
                webView.loadDataWithBaseURL(
                    apiHostUrl,
                    body.replaceLinkTags(isDarkTheme),
                    "text/html",
                    StandardCharsets.UTF_8.name(),
                    null
                )
            }
            contentUrl?.let { url ->
                val currentUrl = webView.url
                val mobileUrl = url.addMobileQueryParam()
                if (currentUrl != mobileUrl && (currentUrl == null || !currentUrl.contains(url))) {
                    if (cookieManager != null) {
                        webView.loadUrl(url, coroutineScope, cookieManager)
                    } else {
                        webView.loadUrl(mobileUrl)
                    }
                }
            }
        },
        onRelease = {
            it.stopLoading()
            it.loadUrl("about:blank")
            it.clearHistory()
            it.removeAllViews()
            it.destroy()
        }
    )
}
