package org.openedx.discovery.presentation.catalog

import android.annotation.SuppressLint
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.openedx.core.extension.addMobileQueryParam
import org.openedx.core.extension.applyFullAccessSettings
import org.openedx.foundation.extension.applyDarkModeIfEnabled
import org.openedx.discovery.presentation.catalog.WebViewLink.Authority as linkAuthority

@SuppressLint("SetJavaScriptEnabled", "ComposableNaming")
@Composable
fun CatalogWebViewScreen(
    url: String,
    uriScheme: String,
    userAgent: String,
    isAllLinksExternal: Boolean = false,
    onWebPageLoaded: () -> Unit,
    refreshSessionCookie: () -> Unit = {},
    onWebPageUpdated: (String) -> Unit = {},
    onUriClick: (String, linkAuthority) -> Unit,
    onWebPageLoadError: () -> Unit
): WebView {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    return remember {
        WebView(context).apply {
            webViewClient = object : DefaultWebViewClient(
                context = context,
                webView = this@apply,
                isAllLinksExternal = isAllLinksExternal,
                onUriClick = onUriClick,
                refreshSessionCookie = refreshSessionCookie,
            ) {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    url?.let { onWebPageUpdated(it) }
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    onWebPageLoaded()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val clickUrl = request?.url?.toString() ?: ""
                    if (handleRecognizedLink(clickUrl)) {
                        return true
                    }

                    return super.shouldOverrideUrlLoading(view, request)
                }

                private fun handleRecognizedLink(clickUrl: String): Boolean {
                    val link = WebViewLink.parse(clickUrl, uriScheme) ?: return false

                    return when (link.authority) {
                        linkAuthority.COURSE_INFO,
                        linkAuthority.PROGRAM_INFO,
                        linkAuthority.ENROLLED_PROGRAM_INFO -> {
                            val pathId = link.params[WebViewLink.Param.PATH_ID] ?: ""
                            onUriClick(pathId, link.authority)
                            true
                        }

                        linkAuthority.ENROLL,
                        linkAuthority.ENROLLED_COURSE_INFO -> {
                            val courseId = link.params[WebViewLink.Param.COURSE_ID] ?: ""
                            onUriClick(courseId, link.authority)
                            true
                        }

                        linkAuthority.COURSE -> {
                            onUriClick("", link.authority)
                            true
                        }

                        else -> false
                    }
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    if (request.url.toString() == view.url) {
                        onWebPageLoadError()
                    }
                    super.onReceivedError(view, request, error)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
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
                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                    if (transport != null) {
                        transport.webView = view
                        resultMsg.sendToTarget()
                        return true
                    }
                    return false
                }
            }

            val isSpecializedUA = applyFullAccessSettings(url)
            if (!isSpecializedUA) {
                settings.userAgentString = "${settings.userAgentString} $userAgent"
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            loadUrl(url.addMobileQueryParam())
            applyDarkModeIfEnabled(isDarkTheme)
        }
    }
}
