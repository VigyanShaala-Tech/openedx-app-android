package org.openedx.profile.presentation.vsprofile

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.presentation.global.AppData
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.profile.R
import org.openedx.core.config.Config
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.profile.presentation.profile.ProfileViewModel

class VsProfileFragment : Fragment() {
    private val router: ProfileRouter by inject()
    private val config: Config by inject()
    private val corePreferences: CorePreferences by inject()
    private val appData: AppData by inject()
    private val settingsViewModel by viewModel<org.openedx.profile.presentation.settings.SettingsViewModel>()
    private val profileViewModel by viewModel<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(profileViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val logoutSuccess by settingsViewModel.successLogout.collectAsState(false)
                val accountData = profileViewModel.uiState.collectAsState().value as? org.openedx.profile.presentation.profile.ProfileUIState.Data
                val isOtpLoading by profileViewModel.isOtpLoading.collectAsState()
                val isOtpSent by profileViewModel.isOtpSent.collectAsState()
                val uiMessage by profileViewModel.uiMessage.observeAsState()

                VsProfileScreen(
                    userName = corePreferences.user?.name ?: corePreferences.user?.username ?: stringResource(id = R.string.profile_student_user),
                    userEmail = corePreferences.user?.email ?: stringResource(id = R.string.profile_student_email),
                    account = accountData?.account,
                    isOtpLoading = isOtpLoading,
                    isOtpSent = isOtpSent,
                    uiMessage = uiMessage,
                    onSettingsClick = { router.navigateToVsSettings(requireActivity().supportFragmentManager) },
                    onAboutClick = { router.navigateToAboutVigyanshaala(requireActivity().supportFragmentManager) },
                    onLogoutClick = { settingsViewModel.logout() },
                    onEditClick = { profileViewModel.profileEditClicked(requireActivity().supportFragmentManager) },
                    onFaqClick = {
                        router.navigateToWebContent(
                            requireActivity().supportFragmentManager,
                            getString(R.string.profile_faqs),
                            config.getFaqUrl()
                        )
                    },
                    onChatWithCurieClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919403509920?text=curie"))
                        startActivity(intent)
                    },
                    onTechnicalSupportClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/+918446846141"))
                        startActivity(intent)
                    },
                    onShareClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "https://play.google.com/store/apps/details?id=${appData.applicationId}"
                            )
                        }
                        startActivity(Intent.createChooser(shareIntent, null))
                    },
                    onSendOtp = { profileViewModel.sendOtp(it) },
                    onVerifyOtp = { phone: String, otp: String -> profileViewModel.verifyOtp(phone, otp) }
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
    account: org.openedx.profile.domain.model.Account?,
    isOtpLoading: Boolean,
    isOtpSent: Boolean,
    uiMessage: UIMessage?,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onChatWithCurieClick: () -> Unit = {},
    onTechnicalSupportClick: () -> Unit = {},
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showWhatsappDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(account?.isWhatsappVerified) {
        if (account?.isWhatsappVerified == true) {
            showWhatsappDialog = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.background),
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

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

        if (showWhatsappDialog) {
            WhatsappDialog(
                initialPhoneNumber = account?.whatsappNumber ?: "",
                isOtpSent = isOtpSent,
                isOtpLoading = isOtpLoading,
                onDismissRequest = { showWhatsappDialog = false },
                onSendOtp = onSendOtp,
                onVerifyOtp = { phone: String, otp: String -> 
                    onVerifyOtp(phone, otp)
                }
            )
        }

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
                            .clickable { onEditClick() },
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
                onClick = onChatWithCurieClick
            )
            VsProfileItem(
                icon = Icons.Filled.HeadsetMic,
                title = stringResource(id = R.string.profile_technical_support),
                onClick = onTechnicalSupportClick
            )
            VsProfileItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = stringResource(id = R.string.profile_help_and_support),
                onClick = {}
            )
            VsProfileItem(
                icon = Icons.Filled.Info,
                title = stringResource(id = R.string.profile_about_vigyanshaala),
                onClick = onAboutClick
            )
            VsProfileItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = stringResource(id = R.string.profile_faqs),
                onClick = onFaqClick
            )
            VsProfileItem(
                icon = Icons.Filled.Share,
                title = stringResource(id = R.string.profile_share_app),
                onClick = onShareClick
            )

//            VsProfileItem(
//                icon = Icons.Outlined.Smartphone,
//                title = if (account?.isWhatsappVerified == true) {
//                    "${stringResource(id = R.string.profile_whatsapp_number)}: ${account.whatsappNumber}"
//                } else {
//                    stringResource(id = R.string.profile_setup_whatsapp)
//                },
//                onClick = { showWhatsappDialog = true }
//            )

            Card(
                backgroundColor = MaterialTheme.appColors.background,
                elevation = 4.dp,
                shape = MaterialTheme.appShapes.cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
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
            Spacer(Modifier.height(16.dp))
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
                    backgroundColor = MaterialTheme.appColors.primary,
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun WhatsappDialog(
    initialPhoneNumber: String,
    isOtpSent: Boolean,
    isOtpLoading: Boolean,
    onDismissRequest: () -> Unit,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
) {
    var phoneNumber by rememberSaveable { mutableStateOf(initialPhoneNumber) }
    var otpCode by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

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
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .semantics { testTagsAsResourceId = true },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = org.openedx.core.R.string.core_cancel),
                            tint = MaterialTheme.appColors.primary
                        )
                    }
                }
                
                Text(
                    text = stringResource(id = R.string.profile_setup_whatsapp),
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.size(24.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.profile_whatsapp_number)) },
                    placeholder = { Text(stringResource(id = R.string.profile_whatsapp_number_placeholder)) },
                    enabled = !isOtpSent && !isOtpLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.appColors.textFieldText,
                        backgroundColor = MaterialTheme.appColors.textFieldBackground,
                        unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                        focusedBorderColor = MaterialTheme.appColors.primary
                    ),
                    shape = MaterialTheme.appShapes.textFieldShape
                )

                if (isOtpSent) {
                    Spacer(Modifier.size(16.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(id = R.string.profile_enter_otp)) },
                        enabled = !isOtpLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.appColors.textFieldText,
                            backgroundColor = MaterialTheme.appColors.textFieldBackground,
                            unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                            focusedBorderColor = MaterialTheme.appColors.primary
                        ),
                        shape = MaterialTheme.appShapes.textFieldShape
                    )
                }

                Spacer(Modifier.size(24.dp))

                OpenEdXButton(
                    text = if (isOtpSent) stringResource(id = R.string.profile_verify_otp) else stringResource(id = R.string.profile_send_otp),
                    onClick = {
                        if (isOtpSent) {
                            onVerifyOtp(phoneNumber, otpCode)
                        } else {
                            onSendOtp(phoneNumber)
                        }
                    },
                    enabled = !isOtpLoading && (if (isOtpSent) otpCode.length >= 4 else (phoneNumber.length >= 10 && phoneNumber != initialPhoneNumber))
                ) {
                    if (isOtpLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = if (isOtpSent) stringResource(id = R.string.profile_verify_otp) else stringResource(id = R.string.profile_send_otp),
                            color = Color.White,
                            style = MaterialTheme.appTypography.labelLarge
                        )
                    }
                }
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VsProfileScreenPreview() {
    OpenEdXTheme {
        VsProfileScreen(
            userName = "Student User",
            userEmail = "student@example.com",
            account = null,
            isOtpLoading = false,
            isOtpSent = false,
            uiMessage = null,
            onSettingsClick = {},
            onAboutClick = {},
            onLogoutClick = {},
            onSendOtp = {},
            onVerifyOtp = { _, _ -> }
        )
    }
}
