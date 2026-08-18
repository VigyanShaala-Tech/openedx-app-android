package org.openedx.course.presentation.unit.html

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.AppDataConstants
import org.openedx.core.extension.addMobileQueryParam
import org.openedx.core.extension.applyFullAccessSettings
import org.openedx.core.extension.loadUrl
import org.openedx.core.system.AppCookieManager
import org.openedx.core.ui.FullScreenErrorView
import org.openedx.core.ui.roundBorderWithoutBottom
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.utils.EmailUtil
import org.openedx.foundation.extension.applyDarkModeIfEnabled
import org.openedx.foundation.extension.isEmailValid
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue

class HtmlUnitFragment : Fragment() {

    private val viewModel by viewModel<HtmlUnitViewModel> {
        parametersOf(
            requireArguments().getString(ARG_BLOCK_ID, ""),
            requireArguments().getString(ARG_COURSE_ID, "")
        )
    }
    private var blockUrl: String = ""
    private var offlineUrl: String = ""
    private var lastModified: String = ""
    private var fromDownloadedContent: Boolean = false

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            filePathCallback?.onReceiveValue(results)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockUrl = requireArguments().getString(ARG_BLOCK_URL, "")
        offlineUrl = requireArguments().getString(ARG_OFFLINE_URL, "")
        lastModified = requireArguments().getString(ARG_LAST_MODIFIED, "")
        fromDownloadedContent = lastModified.isNotEmpty()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            HtmlUnitView(
                viewModel = viewModel,
                blockUrl = blockUrl,
                offlineUrl = offlineUrl,
                fromDownloadedContent = fromDownloadedContent,
                isFragmentAdded = isAdded,
                onShowFileChooser = { callback, params ->
                    filePathCallback = callback
                    try {
                        val intent = params.createIntent()
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (intent.resolveActivity(requireContext().packageManager) != null) {
                            fileChooserLauncher.launch(intent)
                        } else {
                            // Fallback for some devices/OS versions
                            val backupIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            fileChooserLauncher.launch(
                                Intent.createChooser(
                                    backupIntent,
                                    getString(org.openedx.core.R.string.core_file_chooser_title)
                                )
                            )
                        }
                    } catch (_: Exception) {
                        filePathCallback?.onReceiveValue(null)
                        filePathCallback = null
                    }
                }
            )
        }
    }

    companion object {
        private const val ARG_BLOCK_ID = "blockId"
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_BLOCK_URL = "blockUrl"
        private const val ARG_OFFLINE_URL = "offlineUrl"
        private const val ARG_LAST_MODIFIED = "lastModified"
        fun newInstance(
            blockId: String,
            blockUrl: String,
            courseId: String,
            offlineUrl: String = "",
            lastModified: String = ""
        ): HtmlUnitFragment {
            val fragment = HtmlUnitFragment()
            fragment.arguments = bundleOf(
                ARG_BLOCK_ID to blockId,
                ARG_BLOCK_URL to blockUrl,
                ARG_OFFLINE_URL to offlineUrl,
                ARG_LAST_MODIFIED to lastModified,
                ARG_COURSE_ID to courseId
            )
            return fragment
        }
    }
}

@Composable
fun HtmlUnitView(
    viewModel: HtmlUnitViewModel,
    blockUrl: String,
    offlineUrl: String,
    fromDownloadedContent: Boolean,
    isFragmentAdded: Boolean,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Unit,
) {
    OpenEdXTheme {
        val context = LocalContext.current
        val windowSize = rememberWindowSize()

        var hasInternetConnection by remember {
            mutableStateOf(viewModel.isOnline)
        }

        val url by rememberSaveable {
            mutableStateOf(
                if (!hasInternetConnection && offlineUrl.isNotEmpty()) {
                    offlineUrl
                } else {
                    blockUrl
                }
            )
        }

        val injectJSList by viewModel.injectJSList.collectAsState()
        val uiState by viewModel.uiState.collectAsState()

        val configuration = LocalConfiguration.current

        val isMeetingUrl = url.contains("zoom.us") || url.contains("/meeting/") || url.contains("/join/")
        val bottomPadding =
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT && !isMeetingUrl) {
                72.dp
            } else {
                0.dp
            }

        val border = if (!isSystemInDarkTheme() && !viewModel.isCourseUnitProgressEnabled) {
            Modifier.roundBorderWithoutBottom(
                borderWidth = 2.dp,
                cornerRadius = 30.dp
            )
        } else {
            Modifier
        }

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = MaterialTheme.colors.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPadding)
                    .then(border),
                contentAlignment = Alignment.TopCenter
            ) {
                if (uiState is HtmlUnitUIState.Initialization) return@Box
                if ((uiState is HtmlUnitUIState.Error).not()) {
                    if (hasInternetConnection || fromDownloadedContent) {
                        HTMLContentView(
                            uiState = uiState,
                            windowSize = windowSize,
                            url = url,
                            cookieManager = viewModel.cookieManager,
                            apiHostURL = viewModel.apiHostURL,
                            isLoading = uiState is HtmlUnitUIState.Loading,
                            injectJSList = injectJSList,
                            onCompletionSet = {
                                viewModel.notifyCompletionSet()
                            },
                            onWebPageLoading = {
                                viewModel.onWebPageLoading()
                            },
                            onWebPageLoaded = {
                                if ((uiState is HtmlUnitUIState.Error).not()) {
                                    viewModel.onWebPageLoaded()
                                }
                                if (isFragmentAdded) viewModel.setWebPageLoaded(context.assets)
                            },
                            onWebPageLoadError = {
                                if (!fromDownloadedContent) viewModel.onWebPageLoadError()
                            },
                            saveXBlockProgress = { jsonProgress ->
                                viewModel.saveXBlockProgress(jsonProgress)
                            },
                            onShowFileChooser = onShowFileChooser
                        )
                    } else {
                        viewModel.onWebPageLoadError()
                    }
                } else {
                    val errorType = (uiState as HtmlUnitUIState.Error).errorType
                    FullScreenErrorView(errorType = errorType) {
                        hasInternetConnection = viewModel.isOnline
                        viewModel.onWebPageLoading()
                    }
                }
                if (uiState is HtmlUnitUIState.Loading && hasInternetConnection) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                    }
                }
            }
        }
    }
}

@Composable
@SuppressLint("SetJavaScriptEnabled")
private fun HTMLContentView(
    uiState: HtmlUnitUIState,
    windowSize: WindowSize,
    url: String,
    cookieManager: AppCookieManager,
    apiHostURL: String,
    isLoading: Boolean,
    injectJSList: List<String>,
    onCompletionSet: () -> Unit,
    onWebPageLoading: () -> Unit,
    onWebPageLoaded: () -> Unit,
    onWebPageLoadError: () -> Unit,
    saveXBlockProgress: (String) -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val screenWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                compact = Modifier.fillMaxWidth()
            )
        )
    }

    val isDarkTheme = isSystemInDarkTheme()

    AndroidView(
        modifier = Modifier
            .then(screenWidth)
            .background(MaterialTheme.appColors.background),
        factory = {
            WebView(context).apply {
                addJavascriptInterface(
                    object {
                        @Suppress("unused")
                        @JavascriptInterface
                        fun completionSet() {
                            onCompletionSet()
                        }
                    },
                    "callback"
                )
                addJavascriptInterface(
                    JSBridge(
                        postMessageCallback = {
                            coroutineScope.launch {
                                saveXBlockProgress(it)
                                setupOfflineProgress(it)
                            }
                        }
                    ),
                    "AndroidBridge"
                )
                webViewClient = object : WebViewClient() {

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onWebPageLoading()
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        super.onPageCommitVisible(view, url)
                        Log.d("HTML", "onPageCommitVisible")
                        onWebPageLoaded()
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val clickUrl = request?.url?.toString() ?: ""
                        if (clickUrl.startsWith("http://") || clickUrl.startsWith("https://")) {
                            return false
                        }
                        return try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl))
                            context.startActivity(intent)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView,
                        request: WebResourceRequest,
                        errorResponse: WebResourceResponse,
                    ) {
                        if (request.url.toString().startsWith(apiHostURL)) {
                            when (errorResponse.statusCode) {
                                403, 401, 404 -> {
                                    coroutineScope.launch {
                                        cookieManager.tryToRefreshSessionCookie()
                                        loadUrl(url.addMobileQueryParam())
                                    }
                                }
                            }
                        }
                        super.onReceivedHttpError(view, request, errorResponse)
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
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        if (filePathCallback != null && fileChooserParams != null) {
                            onShowFileChooser(filePathCallback, fileChooserParams)
                        }
                        return true
                    }

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
                        transport?.webView = WebView(context)
                        resultMsg?.sendToTarget()
                        return true
                    }

                    override fun onHideCustomView() {
                        // Handled by activity or automatically
                    }
                }
                @Suppress("DEPRECATION")
                applyFullAccessSettings(url)
                with(settings) {
                    mediaPlaybackRequiresUserGesture = false
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                isFocusable = true
                isFocusableInTouchMode = true

                loadUrl(url, coroutineScope, cookieManager)
                applyDarkModeIfEnabled(isDarkTheme)
            }
        },
        update = { webView ->
            if (!isLoading && injectJSList.isNotEmpty()) {
                injectJSList.forEach { webView.evaluateJavascript(it, null) }
                val jsonProgress = (uiState as? HtmlUnitUIState.Loaded)?.jsonProgress
                if (!jsonProgress.isNullOrEmpty()) {
                    webView.setupOfflineProgress(jsonProgress)
                }
            }
        }
    )
}

private fun WebView.setupOfflineProgress(jsonProgress: String) {
    loadUrl("javascript:markProblemCompleted('$jsonProgress');")
}

class JSBridge(val postMessageCallback: (String) -> Unit) {
    @Suppress("unused")
    @JavascriptInterface
    fun postMessage(str: String) {
        postMessageCallback(str)
    }
}
