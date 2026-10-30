package org.openedx.core.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
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
    shareText: String? = null,
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
                        val context = LocalContext.current
                        Toolbar(
                            label = title ?: "",
                            canShowBackBtn = canShowBackBtn,
                            canShowShareIcon = !contentUrl.isNullOrEmpty(),
                            onBackClick = onBackClick,
                            onShareClick = {
                                contentUrl?.let { url ->
                                    val textToShare = if (!shareText.isNullOrBlank()) {
                                        shareText
                                    } else {
                                        url
                                    }
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, title ?: "")
                                        putExtra(Intent.EXTRA_TEXT, textToShare)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(org.openedx.core.R.string.core_share)))
                                }
                            }
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

                    // Handle social media and custom share intents (WhatsApp, Twitter, LinkedIn, Facebook, mailto, tel, intent)
                    if (clickUrl.startsWith("whatsapp://") ||
                        clickUrl.startsWith("intent://") ||
                        clickUrl.startsWith("tel:") ||
                        clickUrl.startsWith("mailto:") ||
                        clickUrl.contains("facebook.com/sharer") ||
                        clickUrl.contains("twitter.com/intent") ||
                        clickUrl.contains("linkedin.com/sharing")
                    ) {
                        try {
                            val intent = if (clickUrl.startsWith("mailto:")) {
                                Intent(Intent.ACTION_SENDTO, Uri.parse(clickUrl))
                            } else if (clickUrl.startsWith("intent://")) {
                                Intent.parseUri(clickUrl, Intent.URI_INTENT_SCHEME)
                            } else {
                                Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl))
                            }
                            context.startActivity(intent)
                            return true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return false
                        }
                    }

                    // Direct PDF / download link clicks
                    val isDownloadOrPdf = clickUrl.endsWith(".pdf", ignoreCase = true) ||
                            clickUrl.endsWith(".png", ignoreCase = true) ||
                            clickUrl.endsWith(".jpg", ignoreCase = true) ||
                            clickUrl.contains("/download/", ignoreCase = true)

                    if (isDownloadOrPdf) {
                        triggerDownload(view, context, clickUrl)
                        return true
                    }

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
                    } else {
                        false
                    }
                }
            }
            addJavascriptInterface(BlobDownloadInterface(context), "AndroidBlobDownloader")
            setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
                triggerDownload(
                    webView = this,
                    context = context,
                    downloadUrl = downloadUrl,
                    userAgent = userAgent,
                    contentDisposition = contentDisposition,
                    mimeType = mimeType
                )
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
                    return false
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

private class BlobDownloadInterface(private val context: Context) {
    @android.webkit.JavascriptInterface
    fun getBase64FromBlobData(base64Data: String, mimeType: String, fileName: String?) {
        try {
            val cleanBase64 = if (base64Data.contains(",")) {
                base64Data.substring(base64Data.indexOf(",") + 1)
            } else {
                base64Data
            }
            val fileBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            val type = if (mimeType.isNotBlank()) mimeType else "application/pdf"
            val name = if (!fileName.isNullOrBlank()) fileName else "Certificate_${System.currentTimeMillis()}.pdf"

            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = java.io.File(downloadsDir, name)
            java.io.FileOutputStream(file).use { out ->
                out.write(fileBytes)
            }

            try {
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (publicDir.exists() || publicDir.mkdirs()) {
                    val publicFile = java.io.File(publicDir, name)
                    java.io.FileOutputStream(publicFile).use { out ->
                        out.write(fileBytes)
                    }
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        arrayOf(publicFile.absolutePath),
                        arrayOf(type),
                        null
                    )
                }
            } catch (_: Exception) {}

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Certificate downloaded to Downloads folder",
                    Toast.LENGTH_LONG
                ).show()

                try {
                    val contentUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(contentUri, type)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(openIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun triggerDownload(
    webView: WebView? = null,
    context: Context,
    downloadUrl: String,
    userAgent: String? = null,
    contentDisposition: String? = null,
    mimeType: String? = null
) {
    try {
        if (downloadUrl.startsWith("blob:")) {
            val js = """
                javascript:(function() {
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', '$downloadUrl', true);
                    xhr.responseType = 'blob';
                    xhr.onload = function(e) {
                        if (this.status == 200) {
                            var blob = this.response;
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                AndroidBlobDownloader.getBase64FromBlobData(base64data, blob.type || 'application/pdf', 'Certificate.pdf');
                            }
                        }
                    };
                    xhr.send();
                })();
            """.trimIndent()
            webView?.evaluateJavascript(js, null)
            return
        }

        if (downloadUrl.startsWith("data:")) {
            val base64Data = if (downloadUrl.contains(",")) {
                downloadUrl.substring(downloadUrl.indexOf(",") + 1)
            } else {
                downloadUrl
            }
            val blobInterface = BlobDownloadInterface(context)
            blobInterface.getBase64FromBlobData(base64Data, mimeType ?: "application/pdf", "Certificate.pdf")
            return
        }

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            val type = if (!mimeType.isNullOrEmpty()) mimeType else "application/pdf"
            setMimeType(type)
            if (!userAgent.isNullOrEmpty()) {
                addRequestHeader("User-Agent", userAgent)
            }
            val cookie = CookieManager.getInstance().getCookie(downloadUrl)
            if (!cookie.isNullOrEmpty()) {
                addRequestHeader("Cookie", cookie)
            }
            val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, type)
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, org.openedx.core.R.string.core_downloading, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
