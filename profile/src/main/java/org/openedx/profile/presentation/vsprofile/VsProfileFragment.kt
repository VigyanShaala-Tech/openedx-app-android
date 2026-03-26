package org.openedx.profile.presentation.vsprofile

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.R
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
                    userName = corePreferences.user?.username ?: stringResource(id = R.string.profile_student_user),
                    userEmail = corePreferences.user?.email ?: stringResource(id = R.string.profile_student_email),
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
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.profile_title),
                style = MaterialTheme.appTypography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.appColors.textDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                backgroundColor = MaterialTheme.appColors.background,
                elevation = 4.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.appColors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.textPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.size(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.appTypography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.appColors.textDark
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = userEmail,
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textPrimaryVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            VsProfileItem(
                icon = Icons.Filled.Settings,
                title = stringResource(id = R.string.profile_settings),
                onClick = onSettingsClick
            )
            VsProfileItem(
                icon = Icons.Filled.ChatBubbleOutline,
                title = stringResource(id = R.string.profile_chat_with_curie),
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.HeadsetMic,
                title = stringResource(id = R.string.profile_technical_support),
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = stringResource(id = R.string.profile_help_and_support),
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Info,
                title = stringResource(id = R.string.profile_about_vigyanshaala),
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = stringResource(id = R.string.profile_faqs),
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Share,
                title = stringResource(id = R.string.profile_share_app),
                onClick = {}
            )

            Card(
                backgroundColor = MaterialTheme.appColors.background,
                elevation = 4.dp,
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
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.error
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = stringResource(id = R.string.profile_logout),
                        style = MaterialTheme.appTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.appColors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun VsProfileItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        backgroundColor = MaterialTheme.appColors.background,
        elevation = 4.dp,
        shape = MaterialTheme.appShapes.cardShape,
        modifier = modifier
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
                tint = MaterialTheme.appColors.textPrimaryVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(16.dp))
            Text(
                text = title,
                style = MaterialTheme.appTypography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.appColors.textDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.appColors.textPrimaryVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VsProfileScreenPreview() {
    OpenEdXTheme {
        VsProfileScreen(
            userName = "Student User",
            userEmail = "student@example.com",
            onSettingsClick = {},
            onLogoutClick = {}
        )
    }
}
