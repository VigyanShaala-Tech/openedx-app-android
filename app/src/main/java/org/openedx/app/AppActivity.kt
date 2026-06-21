package org.openedx.app

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowMetricsCalculator
import com.braze.support.toStringMap
import io.branch.referral.Branch
import io.branch.referral.Branch.BranchUniversalReferralInitListener
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.app.databinding.ActivityAppBinding
import org.openedx.app.deeplink.DeepLink
import org.openedx.auth.data.model.AccountActivationResponse
import org.openedx.auth.presentation.logistration.LogistrationFragment
import org.openedx.auth.presentation.signin.SignInFragment
import org.openedx.core.ApiConstants
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.presentation.global.InsetHolder
import org.openedx.core.presentation.global.WindowSizeHolder
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.utils.Logger
import org.openedx.core.worker.CalendarSyncScheduler
import org.openedx.foundation.extension.requestApplyInsetsWhenAttached
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.whatsnew.WhatsNewManager
import org.openedx.whatsnew.presentation.whatsnew.WhatsNewFragment

class AppActivity : AppCompatActivity(), InsetHolder, WindowSizeHolder {

    override val topInset: Int
        get() = _insetTop
    override val bottomInset: Int
        get() = _insetBottom
    override val cutoutInset: Int
        get() = _insetCutout

    override val windowSize: WindowSize
        get() = _windowSize

    private lateinit var binding: ActivityAppBinding
    private val viewModel by viewModel<AppViewModel>()
    private val whatsNewManager by inject<WhatsNewManager>()
    private val corePreferencesManager by inject<CorePreferences>()
    private val profileRouter by inject<ProfileRouter>()
    private val downloadDialogManager by inject<DownloadDialogManager>()
    private val calendarSyncScheduler by inject<CalendarSyncScheduler>()

    private val branchLogger = Logger(BRANCH_TAG)

    private var _insetTop = 0
    private var _insetBottom = 0
    private var _insetCutout = 0

    private var _windowSize = WindowSize(WindowType.Compact, WindowType.Compact)
    private val authCode: String?
        get() {
            val data = intent?.data
            if (
                data is Uri &&
                data.scheme == BuildConfig.APPLICATION_ID &&
                data.host == ApiConstants.BrowserLogin.REDIRECT_HOST
            ) {
                return data.getQueryParameter(ApiConstants.BrowserLogin.CODE_QUERY_PARAM)
            }
            return null
        }

    private val branchCallback =
        BranchUniversalReferralInitListener { branchUniversalObject, _, error ->
            if (branchUniversalObject?.contentMetadata?.customMetadata != null) {
                branchLogger.i { "Branch init complete." }
                branchLogger.i { branchUniversalObject.contentMetadata.customMetadata.toString() }
                viewModel.makeExternalRoute(
                    fm = supportFragmentManager,
                    deepLink = DeepLink(branchUniversalObject.contentMetadata.customMetadata)
                )
            } else if (error != null) {
                branchLogger.e { "Branch init failed. Caused by -" + error.message }
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(TOP_INSET, topInset)
        outState.putInt(BOTTOM_INSET, bottomInset)
        outState.putInt(CUTOUT_INSET, cutoutInset)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityAppBinding.inflate(layoutInflater)
        lifecycle.addObserver(viewModel)
        viewModel.logAppLaunchEvent()
        setContentView(binding.root)

        setupWindowInsets(savedInstanceState)
        setupWindowSettings()
        setupInitialFragment(savedInstanceState)
        observeLogoutEvent()
        observeDownloadFailedDialog()
        observeAccountActivation()

        calendarSyncScheduler.scheduleDailySync()

        handleDeepLink(intent?.data)
    }

    private fun handleDeepLink(data: Uri?) {
        if (data == null) return
        val isVigyanShaalaDeepLink =
            (data.host == "apps.uat.vigyanshaala.com" && (data.path?.contains("learner-dashboard") == true || data.path?.contains(
                "authn"
            ) == true || data.path?.contains("learning") == true)) ||
                    (data.host == "uat.vigyanshaala.com" && (data.path?.contains("dashboard") == true || data.path?.contains(
                        "courses"
                    ) == true || data.path?.contains("register") == true || data.path?.contains("activate") == true || data.path?.contains(
                        "login"
                    ) == true)) ||
                    (data.scheme == BuildConfig.APPLICATION_ID && data.host == "open")

        if (isVigyanShaalaDeepLink) {
            val screen = data.getQueryParameter("scr")
            if (screen != null) {
                val params = mutableMapOf<String, String>()
                params[DeepLink.Keys.SCREEN_NAME.value] = screen
                data.queryParameterNames.forEach { name ->
                    data.getQueryParameter(name)?.let { value ->
                        params[name] = if (name == "CId" || name == "course_id") {
                            value.replace(" ", "+")
                        } else {
                            value
                        }
                    }
                }

                // Extract courseId from path if missing in query
                if (!params.containsKey(DeepLink.Keys.COURSE_ID.value) &&
                    !params.containsKey(DeepLink.Keys.COURSE_ID_ALT.value)
                ) {
                    val segments = data.pathSegments
                    if (segments.size >= 2 && segments[0] == "courses") {
                        params[DeepLink.Keys.COURSE_ID.value] = segments[1].replace(" ", "+")
                    }
                }

                // Extract token for PasswordReset from path if missing in query
                if (screen == "PasswordReset" && !params.containsKey(DeepLink.Keys.TOKEN.value)) {
                    val segments = data.pathSegments
                    if (segments.size >= 3 && segments[1] == "password_reset_confirm") {
                        params[DeepLink.Keys.TOKEN.value] = segments[2]
                    }
                }

                // Extract activationId for accActivation from path if missing in query
                if (screen == "accActivation") {
                    val id = if (!params.containsKey(DeepLink.Keys.ACTIVATION_ID.value)) {
                        val segments = data.pathSegments
                        if (segments.size >= 2 && segments[0] == "activate") {
                            segments[1]
                        } else {
                            ""
                        }
                    } else {
                        params[DeepLink.Keys.ACTIVATION_ID.value] ?: ""
                    }
                    if (id.isNotEmpty()) {
                        viewModel.activateAccount(id)
                        return
                    }
                }

                // Extract meetingId from path if missing in query
                if (screen == "meeting") {
                    val segments = data.pathSegments
                    if (segments.size >= 6 && segments[4] == "join") {
                        if (!params.containsKey(DeepLink.Keys.MEETING_ID.value)) {
                            params[DeepLink.Keys.MEETING_ID.value] = segments[5]
                        }
                        // For meeting links, path course ID is often more reliable than CId query param
                        if (segments[1] == "course" && segments[2].startsWith("course-v1:")) {
                            params[DeepLink.Keys.COURSE_ID.value] = segments[2].replace(" ", "+")
                        }
                    }
                }

                viewModel.makeExternalRoute(
                    supportFragmentManager,
                    DeepLink(params)
                )
            }
        }
    }

    private fun setupWindowInsets(savedInstanceState: Bundle?) {
        val container = binding.rootLayout
        container.addView(object : View(this) {
            override fun onConfigurationChanged(newConfig: Configuration?) {
                super.onConfigurationChanged(newConfig)
                computeWindowSizeClasses()
            }
        })
        computeWindowSizeClasses()

        savedInstanceState?.let {
            _insetTop = it.getInt(TOP_INSET, 0)
            _insetBottom = it.getInt(BOTTOM_INSET, 0)
            _insetCutout = it.getInt(CUTOUT_INSET, 0)
        }

        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())

            _insetTop = insetsCompat.top
            _insetBottom = insetsCompat.bottom

            val displayCutout = WindowInsetsCompat.toWindowInsetsCompat(insets).displayCutout
            if (displayCutout != null) {
                val top = displayCutout.safeInsetTop
                val left = displayCutout.safeInsetLeft
                val right = displayCutout.safeInsetRight
                _insetCutout = maxOf(top, left, right)
            }

            insets
        }
        binding.root.requestApplyInsetsWhenAttached()
    }

    private fun setupWindowSettings() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            WindowCompat.setDecorFitsSystemWindows(this, false)
            val insetsController = WindowInsetsControllerCompat(this, binding.root)
            insetsController.isAppearanceLightStatusBars = !isUsingNightModeResources()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
            }
        }
    }

    private fun setupInitialFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            when {
                corePreferencesManager.user == null -> {
                    val fragment = if (viewModel.isLogistrationEnabled && authCode == null) {
                        LogistrationFragment()
                    } else {
                        SignInFragment.newInstance(null, null, authCode = authCode)
                    }
                    addFragment(fragment)
                }

                whatsNewManager.shouldShowWhatsNew() -> addFragment(WhatsNewFragment.newInstance())
                else -> addFragment(MainFragment.newInstance())
            }

            intent.extras?.takeIf { it.containsKey(DeepLink.Keys.NOTIFICATION_TYPE.value) }?.let {
                handlePushNotification(it)
            }
        }
    }

    private fun observeLogoutEvent() {
        viewModel.logoutUser.observe(this) {
            profileRouter.restartApp(supportFragmentManager, viewModel.isLogistrationEnabled)
        }
    }

    private fun observeDownloadFailedDialog() {
        lifecycleScope.launch {
            viewModel.downloadFailedDialog.collect {
                downloadDialogManager.showDownloadFailedPopup(
                    downloadModel = it.downloadModel,
                    fragmentManager = supportFragmentManager,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.isBranchEnabled) {
            Branch.sessionBuilder(this)
                .withCallback(branchCallback)
                .withData(this.intent.data)
                .init()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent

        handleDeepLink(intent.data)

        if (authCode != null) {
            addFragment(SignInFragment.newInstance(null, null, authCode = authCode))
        }

        val extras = intent.extras
        if (extras?.containsKey(DeepLink.Keys.NOTIFICATION_TYPE.value) == true) {
            handlePushNotification(extras)
        }

        if (viewModel.isBranchEnabled) {
            if (intent.getBooleanExtra(BRANCH_FORCE_NEW_SESSION, false)) {
                Branch.sessionBuilder(this)
                    .withCallback(branchCallback)
                    .reInit()
            }
        }
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.container, fragment)
            .commit()
    }

    private fun computeWindowSizeClasses() {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        val widthDp = metrics.bounds.width() / resources.displayMetrics.density
        val widthWindowSize = when {
            widthDp < COMPACT_MAX_WIDTH -> WindowType.Compact
            widthDp < MEDIUM_MAX_WIDTH -> WindowType.Medium
            else -> WindowType.Expanded
        }

        val heightDp = metrics.bounds.height() / resources.displayMetrics.density
        val heightWindowSize = when {
            heightDp < COMPACT_MAX_HEIGHT -> WindowType.Compact
            heightDp < MEDIUM_MAX_HEIGHT -> WindowType.Medium
            else -> WindowType.Expanded
        }
        _windowSize = WindowSize(widthWindowSize, heightWindowSize)
    }

    private fun isUsingNightModeResources(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    private fun observeAccountActivation() {
        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        binding.rootLayout.addView(composeView)

        composeView.setContent {
            OpenEdXTheme {
                val response by viewModel.accountActivationResponse.collectAsState()
                val isLoading by viewModel.isActivationLoading.collectAsState()

                if (isLoading || response != null) {
                    ActivationDialog(
                        response = response,
                        isLoading = isLoading,
                        onDismiss = {
                            viewModel.clearActivationResponse()
                        },
                        onLoginClick = {
                            viewModel.clearActivationResponse()
                            profileRouter.restartApp(
                                supportFragmentManager,
                                viewModel.isLogistrationEnabled
                            )
                        }
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiMessage.collect {
                if (it is UIMessage.SnackBarMessage) {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        it.message,
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @Composable
    private fun ActivationDialog(
        response: AccountActivationResponse?,
        isLoading: Boolean,
        onDismiss: () -> Unit,
        onLoginClick: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.appColors.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                } else if (response != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val icon = when (response.status) {
                            "success" -> Icons.Default.CheckCircle
                            "info" -> Icons.Default.Info
                            else -> Icons.Default.Error
                        }
                        val iconColor = when (response.status) {
                            "success" -> Color(0xFF4CAF50)
                            "info" -> MaterialTheme.appColors.primary
                            else -> MaterialTheme.appColors.error
                        }

                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = iconColor.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = when (response.status) {
                                "success" -> "Account Activated"
                                "info" -> "Already Activated"
                                else -> "Activation Failed"
                            },
                            style = MaterialTheme.appTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.appColors.textDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = response.message ?: "",
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OpenEdXButton(
                            text = stringResource(id = org.openedx.core.R.string.core_sign_in),
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    private fun handlePushNotification(data: Bundle) {
        val deepLink = DeepLink(data.toStringMap())
        viewModel.makeExternalRoute(supportFragmentManager, deepLink)
    }

    companion object {
        const val TOP_INSET = "topInset"
        const val BOTTOM_INSET = "bottomInset"
        const val CUTOUT_INSET = "cutoutInset"
        const val BRANCH_TAG = "Branch"
        const val BRANCH_FORCE_NEW_SESSION = "branch_force_new_session"

        internal const val COMPACT_MAX_WIDTH = 600
        internal const val MEDIUM_MAX_WIDTH = 840
        internal const val COMPACT_MAX_HEIGHT = 480
        internal const val MEDIUM_MAX_HEIGHT = 900
    }
}
