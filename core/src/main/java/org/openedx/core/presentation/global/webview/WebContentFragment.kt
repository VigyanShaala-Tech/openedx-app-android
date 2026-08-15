package org.openedx.core.presentation.global.webview

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.openedx.core.config.Config
import org.openedx.core.presentation.dialog.MeetingExitDialogListener
import org.openedx.core.presentation.dialog.MeetingExitFragmentDialog
import org.openedx.core.system.notifier.MeetingNotifier
import org.openedx.core.ui.WebContentScreen
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.foundation.presentation.rememberWindowSize

class WebContentFragment : Fragment() {

    private val config: Config by inject()
    private val meetingNotifier: MeetingNotifier by inject()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val url = requireArguments().getString(ARG_URL, "")
            val isMeeting = org.openedx.core.AppDataConstants.ZOOM_URL_PATTERNS.any { url.contains(it) }
            if (isMeeting) {
                val dialog = MeetingExitFragmentDialog.newInstance()
                dialog.listener = object : MeetingExitDialogListener {
                    override fun onConfirm() {
                        requireActivity().supportFragmentManager.popBackStack()
                    }

                    override fun onMinimize() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val paramsBuilder = PictureInPictureParams.Builder()
                                .setAspectRatio(Rational(16, 9))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                paramsBuilder.setAutoEnterEnabled(true)
                                paramsBuilder.setSeamlessResizeEnabled(true)
                            }
                            try {
                                requireActivity().enterPictureInPictureMode(paramsBuilder.build())
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    }
                }
                dialog.show(
                    requireActivity().supportFragmentManager,
                    MeetingExitFragmentDialog::class.simpleName
                )
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        val url = requireArguments().getString(ARG_URL, "")
        val isMeeting = org.openedx.core.AppDataConstants.ZOOM_URL_PATTERNS.any { url.contains(it) }
        if (isMeeting) {
            lifecycleScope.launch {
                meetingNotifier.send(true)
            }
        }
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()
                WebContentScreen(
                    apiHostUrl = config.getApiHostURL(),
                    windowSize = windowSize,
                    title = requireArguments().getString(ARG_TITLE, ""),
                    contentUrl = requireArguments().getString(ARG_URL, ""),
                    onBackClick = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            meetingNotifier.send(false)
        }
        super.onDestroy()
        CookieManager.getInstance().flush()
    }

    companion object {
        private const val ARG_TITLE = "argTitle"
        private const val ARG_URL = "argUrl"

        fun newInstance(title: String, url: String): WebContentFragment {
            val fragment = WebContentFragment()
            fragment.arguments = bundleOf(
                ARG_TITLE to title,
                ARG_URL to url,
            )
            return fragment
        }
    }
}
