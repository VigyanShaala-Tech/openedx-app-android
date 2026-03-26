package org.openedx.profile.presentation.vsprofile

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.R
import org.openedx.profile.presentation.settings.SettingsUIState
import org.openedx.profile.presentation.settings.SettingsViewModel

class VsSettingsFragment : Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val logoutSuccess by viewModel.successLogout.collectAsState(false)
                val uiState by viewModel.uiState.collectAsState()

                VsSettingsScreen(
                    versionName = (uiState as? SettingsUIState.Data)?.configuration?.versionName ?: "1.0.0",
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    onManageAccountClick = {
                        viewModel.manageAccountClicked(requireActivity().supportFragmentManager)
                    },
                    onVideoClick = {
                        viewModel.videoSettingsClicked(requireActivity().supportFragmentManager)
                    },
                    onCalendarClick = {
                        viewModel.calendarSettingsClicked(requireActivity().supportFragmentManager)
                    },
                    onLogoutClick = {
                        viewModel.logout()
                    }
                )

                androidx.compose.runtime.LaunchedEffect(logoutSuccess) {
                    if (logoutSuccess) {
                        viewModel.restartApp(requireActivity().supportFragmentManager)
                    }
                }
            }
        }
    }
}

@Composable
private fun VsSettingsScreen(
    versionName: String,
    onBackClick: () -> Unit,
    onManageAccountClick: () -> Unit,
    onVideoClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.background),
        backgroundColor = MaterialTheme.appColors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF689F38))
                    .statusBarsInset()
            ) {
                Toolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .displayCutoutForLandscape(),
                    label = stringResource(id = org.openedx.core.R.string.core_settings),
                    canShowBackBtn = true,
                    onBackClick = onBackClick,
                    labelTint = Color.White,
                    iconTint = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (showLogoutDialog) {
            LogoutDialog(
                onDismissRequest = {
                    showLogoutDialog = false
                },
                onLogoutClick = {
                    showLogoutDialog = false
                    onLogoutClick()
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                backgroundColor = MaterialTheme.appColors.surface,
                elevation = 4.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                VsSettingsItem(
                    title = stringResource(id = org.openedx.core.R.string.core_manage_account),
                    onClick = onManageAccountClick
                )
            }

            Text(
                text = stringResource(id = org.openedx.core.R.string.core_settings),
                style = MaterialTheme.appTypography.labelLarge,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                backgroundColor = MaterialTheme.appColors.surface,
                elevation = 4.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    VsSettingsItem(
                        title = stringResource(id = R.string.profile_video),
                        onClick = onVideoClick
                    )
                    Divider(
                        color = MaterialTheme.appColors.cardViewBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    VsSettingsItem(
                        title = stringResource(id = R.string.profile_dates_and_calendar),
                        onClick = onCalendarClick
                    )
                }
            }

            Card(
                backgroundColor = MaterialTheme.appColors.surface,
                elevation = 4.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                VsSettingsItem(
                    title = stringResource(id = org.openedx.core.R.string.core_version, versionName),
                    showChevron = false
                )
            }

            Card(
                backgroundColor = MaterialTheme.appColors.surface,
                elevation = 4.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_logout),
                        style = MaterialTheme.appTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.appColors.error
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VsSettingsItem(
    title: String,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = showChevron) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.appTypography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            color = MaterialTheme.appColors.textDark,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.appColors.textPrimaryVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LogoutDialog(
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.appColors.background,
                        MaterialTheme.appShapes.cardShape
                    )
                    .clip(MaterialTheme.appShapes.cardShape)
                    .border(
                        1.dp,
                        MaterialTheme.appColors.cardViewBorder,
                        MaterialTheme.appShapes.cardShape
                    )
                    .padding(horizontal = 40.dp, vertical = 36.dp)
                    .semantics { testTagsAsResourceId = true },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        modifier = Modifier
                            .testTag("ib_close")
                            .size(24.dp),
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = org.openedx.core.R.string.core_cancel),
                            tint = MaterialTheme.appColors.primary
                        )
                    }
                }
                Icon(
                    modifier = Modifier
                        .width(88.dp)
                        .height(85.dp),
                    painter = painterResource(R.drawable.profile_ic_exit),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.onBackground
                )
                Spacer(Modifier.size(36.dp))
                Text(
                    modifier = Modifier.testTag("txt_logout_dialog_title"),
                    text = stringResource(id = R.string.profile_logout_dialog_body),
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(36.dp))
                OpenEdXButton(
                    text = stringResource(id = R.string.profile_logout),
                    backgroundColor = MaterialTheme.appColors.warning,
                    onClick = onLogoutClick,
                    content = {
                        Box(
                            Modifier
                                .testTag("btn_logout")
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                modifier = Modifier
                                    .testTag("txt_logout")
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.profile_logout),
                                color = MaterialTheme.appColors.textWarning,
                                style = MaterialTheme.appTypography.labelLarge,
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                modifier = Modifier
                                    .testTag("ic_logout"),
                                painter = painterResource(id = R.drawable.profile_ic_logout),
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    }
                )
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VsSettingsScreenPreview() {
    OpenEdXTheme {
        VsSettingsScreen(
            versionName = "1.0.0",
            onBackClick = {},
            onManageAccountClick = {},
            onVideoClick = {},
            onCalendarClick = {},
            onLogoutClick = {}
        )
    }
}
