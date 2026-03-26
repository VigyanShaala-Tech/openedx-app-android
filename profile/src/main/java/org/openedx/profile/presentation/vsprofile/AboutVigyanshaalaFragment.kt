package org.openedx.profile.presentation.vsprofile

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.R
import org.openedx.profile.presentation.settings.SettingsViewModel

class AboutVigyanshaalaFragment : Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                AboutVigyanshaalaScreen(
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    onTermsOfUseClick = {
                        viewModel.termsOfUseClicked(requireActivity().supportFragmentManager)
                    },
                    onPrivacyPolicyClick = {
                        viewModel.privacyPolicyClicked(requireActivity().supportFragmentManager)
                    },
                    onContactUsClick = {
                        viewModel.emailSupportClicked(requireContext())
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutVigyanshaalaScreen(
    onBackClick: () -> Unit,
    onTermsOfUseClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onContactUsClick: () -> Unit
) {
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
                    label = stringResource(id = R.string.profile_about_vigyanshaala),
                    canShowBackBtn = true,
                    onBackClick = onBackClick,
                    labelTint = Color.White,
                    iconTint = Color.White
                )
            }
        }
    ) { paddingValues ->
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
                Column {
                    AboutItem(
                        icon = Icons.Filled.Description,
                        title = stringResource(id = org.openedx.core.R.string.core_terms_of_use),
                        onClick = onTermsOfUseClick
                    )
                    Divider(
                        color = MaterialTheme.appColors.cardViewBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    AboutItem(
                        icon = Icons.Filled.Shield,
                        title = stringResource(id = org.openedx.core.R.string.core_privacy_policy),
                        onClick = onPrivacyPolicyClick
                    )
                    Divider(
                        color = MaterialTheme.appColors.cardViewBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    AboutItem(
                        icon = Icons.Filled.MailOutline,
                        title = "Contact Us",
                        onClick = onContactUsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF607D8B),
            modifier = Modifier.size(24.dp)
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutVigyanshaalaScreenPreview() {
    OpenEdXTheme {
        AboutVigyanshaalaScreen(
            onBackClick = {},
            onTermsOfUseClick = {},
            onPrivacyPolicyClick = {},
            onContactUsClick = {}
        )
    }
}
