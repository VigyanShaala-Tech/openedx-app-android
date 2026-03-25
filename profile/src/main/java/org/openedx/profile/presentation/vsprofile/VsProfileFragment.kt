package org.openedx.profile.presentation.vsprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.presentation.ProfileRouter

class VsProfileFragment : Fragment() {
    private val router: ProfileRouter by inject()
    private val corePreferences: CorePreferences by inject()
    private val settingsViewModel by viewModel<org.openedx.profile.presentation.settings.SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val logoutSuccess by settingsViewModel.successLogout.collectAsState(false)
                VsProfileScreen(
                    userName = corePreferences.user?.username.orEmpty(),
                    userEmail = corePreferences.user?.email.orEmpty(),
                    onSettingsClick = { router.navigateToSettings(requireActivity().supportFragmentManager) },
                    onLogoutClick = { settingsViewModel.logout() }
                )
                androidx.compose.runtime.LaunchedEffect(logoutSuccess) {
                    if (logoutSuccess) {
                        settingsViewModel.restartApp(requireActivity().supportFragmentManager)
                    }
                }
            }
        }
    }
}

@Composable
private fun VsProfileScreen(
    userName: String,
    userEmail: String,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.background),
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsInset()
                .displayCutoutForLandscape()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.appTypography.titleLarge,
                color = MaterialTheme.appColors.textDark
            )
            Card(
                backgroundColor = MaterialTheme.appColors.surface,
                elevation = 0.dp,
                shape = MaterialTheme.appShapes.cardShape,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.appColors.tabUnselectedBtnBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.textPrimary
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName.ifEmpty { "Student User" },
                            style = MaterialTheme.appTypography.titleSmall,
                            color = MaterialTheme.appColors.textDark
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = userEmail.ifEmpty { "student@example.com" },
                            style = MaterialTheme.appTypography.labelSmall,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.textPrimary
                    )
                }
            }

            VsProfileItem(
                icon = Icons.Filled.Settings,
                title = "Settings",
                onClick = onSettingsClick
            )
            VsProfileItem(
                icon = Icons.Filled.ChatBubble,
                title = "Chat with Curie",
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.HeadsetMic,
                title = "Technical Support",
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Help,
                title = "Help & Support",
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Info,
                title = "About Vigyanshaala",
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Help,
                title = "Frequently Asked Questions (FAQs)",
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Share,
                title = "Share the Vigyanshaala App",
                onClick = {}
            )

            Card(
                backgroundColor = MaterialTheme.appColors.surface,
                elevation = 0.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogoutClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.warning
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "Log out",
                        style = MaterialTheme.appTypography.titleSmall,
                        color = MaterialTheme.appColors.warning
                    )
                }
            }
        }
    }
}

@Composable
private fun VsProfileItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = title,
                style = MaterialTheme.appTypography.titleSmall,
                color = MaterialTheme.appColors.textDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.appColors.textPrimary
            )
        }
    }
}
