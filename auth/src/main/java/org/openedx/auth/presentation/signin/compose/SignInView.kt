package org.openedx.auth.presentation.signin.compose

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.auth.R
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInUIState
import org.openedx.auth.presentation.ui.LoginTextField
import org.openedx.auth.presentation.ui.PasswordVisibilityIcon
import org.openedx.auth.presentation.ui.SocialAuthView
import org.openedx.core.extension.TextConverter
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.HyperlinkText
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.compose.SignInLogoView
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as coreR

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginScreen(
    windowSize: WindowSize,
    state: SignInUIState,
    uiMessage: UIMessage?,
    onEvent: (AuthEvent) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .semantics {
                testTagsAsResourceId = true
            },
        backgroundColor = MaterialTheme.appColors.background
    ) {
        val contentPaddings by remember {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier
                        .widthIn(Dp.Unspecified, 420.dp)
                        .padding(
                            top = 32.dp,
                            bottom = 40.dp
                        ),
                    compact = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                )
            )
        }
        val buttonWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(232.dp, Dp.Unspecified),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.3f),
            painter = painterResource(id = coreR.drawable.core_green_gradient_rect),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
        HandleUIMessage(
            uiMessage = uiMessage,
            scaffoldState = scaffoldState
        )
        if (state.isLogistrationEnabled) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                BackBtn(
                    modifier = Modifier.padding(end = 16.dp),
                    tint = Color.White
                ) {
                    onEvent(AuthEvent.BackClick)
                }
            }
        }
        Column(
            Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignInLogoView()
            Surface(
                color = MaterialTheme.appColors.background,
                shape = MaterialTheme.appShapes.screenBackgroundShape,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.appColors.background)
                            .verticalScroll(scrollState)
                            .displayCutoutForLandscape()
                            .then(contentPaddings),
                    ) {
                        Text(
                            modifier = Modifier.testTag("txt_sign_in_title"),
                            text = stringResource(id = coreR.string.core_sign_in),
                            color = MaterialTheme.appColors.textPrimary,
                            style = MaterialTheme.appTypography.displaySmall
                        )
                        Text(
                            modifier = Modifier
                                .testTag("txt_sign_in_description")
                                .padding(top = 4.dp),
                            text = stringResource(id = R.string.auth_welcome_back),
                            color = MaterialTheme.appColors.textPrimary,
                            style = MaterialTheme.appTypography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (state.isSocialAuthEnabled) {
                            SocialAuthView(
                                modifier = buttonWidth,
                                isGoogleAuthEnabled = true,
                                isFacebookAuthEnabled = state.isFacebookAuthEnabled,
                                isMicrosoftAuthEnabled = state.isMicrosoftAuthEnabled,
                                isSignIn = true,
                            ) {
                                onEvent(AuthEvent.SocialSignIn(it))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            DividerWithOrLabel()
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        AuthForm(
                            buttonWidth,
                            state,
                            onEvent,
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Don't have an account? ",
                                style = MaterialTheme.appTypography.bodyMedium,
                                color = MaterialTheme.appColors.textSecondary
                            )
                            Text(
                                text = "Sign up",
                                style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.appColors.primary,
                                modifier = Modifier.noRippleClickable {
                                    onEvent(AuthEvent.RegisterClick)
                                }
                            )
                        }

                        state.agreement?.let {
                            Spacer(modifier = Modifier.height(24.dp))
                            val linkedText =
                                TextConverter.htmlTextToLinkedText(state.agreement.label)
                            HyperlinkText(
                                modifier = Modifier.testTag("txt_${state.agreement.name}"),
                                fullText = linkedText.text,
                                hyperLinks = linkedText.links,
                                linkTextColor = MaterialTheme.appColors.textHyperLink,
                                linkTextDecoration = TextDecoration.Underline,
                                action = { link ->
                                    onEvent(AuthEvent.OpenLink(linkedText.links, link))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthForm(
    buttonWidth: Modifier,
    state: SignInUIState,
    onEvent: (AuthEvent) -> Unit,
) {
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var isOtpSent by rememberSaveable { mutableStateOf(false) }
    var isEmailTab by rememberSaveable { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isEmailError by rememberSaveable { mutableStateOf(false) }
    var isPasswordError by rememberSaveable { mutableStateOf(false) }
    var isMobileError by rememberSaveable { mutableStateOf(false) }
    var isOtpError by rememberSaveable { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!state.isBrowserLoginEnabled) {
            AuthMethodTabs(
                isEmailSelected = isEmailTab,
                onSelectEmail = { isEmailTab = true },
                onSelectOtp = { isEmailTab = false }
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isEmailTab) {
            LoginTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.auth_email),
                description = stringResource(id = R.string.auth_enter_email_username),
                onValueChanged = {
                    login = it
                    isEmailError = false
                },
                isError = isEmailError,
                errorMessages = stringResource(id = R.string.auth_error_empty_username_email),
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(18.dp))
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                onValueChanged = {
                    password = it
                    isPasswordError = false
                },
                onPressDone = {
                    keyboardController?.hide()
                    if (login.isNotEmpty() && password.isNotEmpty()) {
                        onEvent(AuthEvent.SignIn(login = login, password = password))
                    } else {
                        isEmailError = login.isEmpty()
                        isPasswordError = password.isEmpty()
                    }
                },
                isError = isPasswordError,
            )
            } else {
                MobileOtpSection(
                    isOtpSent = isOtpSent,
                    mobile = mobile,
                    otp = otp,
                    onMobileChanged = {
                        mobile = it
                        isMobileError = false
                    },
                    onOtpChanged = {
                        otp = it
                        isOtpError = false
                    },
                    onChangeNumber = {
                        isOtpSent = false
                        otp = ""
                    },
                    isMobileError = isMobileError,
                    isOtpError = isOtpError,
                    secondsLeft = state.otpSecondsLeft,
                    canResend = state.otpCanResend,
                    onResend = {
                        if (mobile.isNotEmpty()) {
                            onEvent(AuthEvent.SendOtp(mobile))
                        } else {
                            isMobileError = true
                        }
                    }
                )
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp)
        ) {
            if (!state.isBrowserLoginEnabled && isEmailTab) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .testTag("txt_forgot_password")
                        .noRippleClickable {
                            onEvent(AuthEvent.ForgotPasswordClick)
                        },
                    text = stringResource(id = R.string.auth_forgot_password),
                    color = MaterialTheme.appColors.infoVariant,
                    style = MaterialTheme.appTypography.labelLarge
                )
            }
        }

        if (state.showProgress) {
            CircularProgressIndicator(color = MaterialTheme.appColors.primary)
        } else {
            if (state.isBrowserLoginEnabled) {
                OpenEdXButton(
                    modifier = buttonWidth.testTag("btn_sign_in"),
                    text = stringResource(id = coreR.string.core_sign_in),
                    textColor = MaterialTheme.appColors.primaryButtonText,
                    backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                    onClick = { onEvent(AuthEvent.SignInBrowser) }
                )
            } else {
                if (isEmailTab) {
                    OpenEdXButton(
                        modifier = buttonWidth.testTag("btn_sign_in"),
                        text = stringResource(id = coreR.string.core_sign_in),
                        textColor = MaterialTheme.appColors.primaryButtonText,
                        backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                        onClick = {
                            keyboardController?.hide()
                            if (login.isNotEmpty() && password.isNotEmpty()) {
                                onEvent(AuthEvent.SignIn(login = login, password = password))
                            } else {
                                isEmailError = login.isEmpty()
                                isPasswordError = password.isEmpty()
                            }
                        }
                    )
                } else {
                    if (!isOtpSent) {
                        OpenEdXButton(
                            modifier = buttonWidth.testTag("btn_send_otp"),
                            text = stringResource(id = R.string.auth_send_otp),
                            textColor = MaterialTheme.appColors.primaryButtonText,
                            backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                            onClick = {
                                keyboardController?.hide()
                                if (mobile.isNotEmpty()) {
                                    onEvent(AuthEvent.SendOtp(mobile))
                                    isOtpSent = true
                                } else {
                                    isMobileError = true
                                }
                            }
                        )
                    } else {
                        OpenEdXButton(
                            modifier = buttonWidth.testTag("btn_verify_otp"),
                            text = stringResource(id = R.string.auth_verify_and_sign_in),
                            textColor = MaterialTheme.appColors.primaryButtonText,
                            backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                            onClick = {
                                keyboardController?.hide()
                                if (otp.isNotEmpty()) {
                                    onEvent(AuthEvent.VerifyOtp(mobile, otp))
                                } else {
                                    isOtpError = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthMethodTabs(
    isEmailSelected: Boolean,
    onSelectEmail: () -> Unit,
    onSelectOtp: () -> Unit,
) {
    val containerColor = MaterialTheme.appColors.textFieldBackground
    val selectedBg = MaterialTheme.appColors.lightTab
    val selectedText = MaterialTheme.appColors.textPrimary
    val unselectedText = MaterialTheme.appColors.textFieldHint

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, shape = MaterialTheme.appShapes.buttonShape)
            .padding(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            ToggleItem(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                text = stringResource(id = R.string.auth_email),
                icon = Icons.Filled.Email,
                isSelected = isEmailSelected,
                selectedBg = selectedBg,
                selectedText = selectedText,
                unselectedText = unselectedText,
                onClick = onSelectEmail
            )
            Spacer(modifier = Modifier.width(8.dp))
            ToggleItem(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                text = stringResource(id = R.string.auth_mobile_otp),
                icon = Icons.Filled.PhoneAndroid,
                isSelected = !isEmailSelected,
                selectedBg = selectedBg,
                selectedText = selectedText,
                unselectedText = unselectedText,
                onClick = onSelectOtp
            )
        }
    }
}

@Composable
private fun MobileOtpSection(
    isOtpSent: Boolean,
    mobile: String,
    otp: String,
    onMobileChanged: (String) -> Unit,
    onOtpChanged: (String) -> Unit,
    onChangeNumber: () -> Unit,
    isMobileError: Boolean,
    isOtpError: Boolean,
    secondsLeft: Int,
    canResend: Boolean,
    onResend: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var mobileTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(mobile))
        }
        var otpTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(otp))
        }
        Text(
            text = stringResource(id = R.string.auth_mobile_number),
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = mobileTextFieldValue,
            onValueChange = {
                val raw = it.text.trim()
                val normalized = if (raw.startsWith("+91")) raw else "+91${raw.removePrefix("+")}"
                mobileTextFieldValue = it.copy(text = normalized, selection = TextRange(normalized.length))
                onMobileChanged(normalized)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.appColors.textFieldText,
                backgroundColor = MaterialTheme.appColors.textFieldBackground,
                unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                cursorColor = MaterialTheme.appColors.textFieldText,
            ),
            shape = MaterialTheme.appShapes.textFieldShape,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.auth_enter_mobile_placeholder),
                    color = MaterialTheme.appColors.textFieldHint,
                    style = MaterialTheme.appTypography.bodyMedium
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            isError = isMobileError,
            modifier = Modifier.fillMaxWidth()
        )
        if (isOtpSent) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(id = R.string.auth_enter_otp),
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = otpTextFieldValue,
                onValueChange = {
                    otpTextFieldValue = it
                    onOtpChanged(it.text.trim())
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.appColors.textFieldText,
                    backgroundColor = MaterialTheme.appColors.textFieldBackground,
                    unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                    cursorColor = MaterialTheme.appColors.textFieldText,
                ),
                shape = MaterialTheme.appShapes.textFieldShape,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.auth_enter_otp_placeholder),
                        color = MaterialTheme.appColors.textFieldHint,
                        style = MaterialTheme.appTypography.bodyMedium
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = isOtpError,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.auth_otp_sent_to),
                    color = MaterialTheme.appColors.textSecondary,
                    style = MaterialTheme.appTypography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                val resendText = if (canResend) stringResource(id = R.string.auth_resend_otp) else "${stringResource(id = R.string.auth_resend_in)} ${secondsLeft}s"
                Text(
                    text = resendText,
                    color = MaterialTheme.appColors.primary,
                    style = MaterialTheme.appTypography.bodySmall,
                    modifier = Modifier.noRippleClickable {
                        if (canResend) onResend()
                    }
                )
            }
        }
    }
}

@Composable
private fun DividerWithOrLabel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material.Divider(
            color = MaterialTheme.appColors.textFieldBorder,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(id = R.string.auth_or),
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        androidx.compose.material.Divider(
            color = MaterialTheme.appColors.textFieldBorder,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToggleItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedBg: Color,
    selectedText: Color,
    unselectedText: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) selectedBg else Color.Transparent
    val tint = if (isSelected) selectedText else unselectedText
    Row(
        modifier = modifier
            .background(bg, shape = MaterialTheme.appShapes.buttonShape)
            .noRippleClickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        androidx.compose.material.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = tint,
            fontSize = 14.sp,
            style = MaterialTheme.appTypography.bodyMedium
        )
    }
}

@Composable
private fun PasswordTextField(
    modifier: Modifier = Modifier,
    isError: Boolean,
    onValueChanged: (String) -> Unit,
    onPressDone: () -> Unit,
) {
    var passwordTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Text(
        modifier = Modifier
            .testTag("txt_password_label")
            .fillMaxWidth(),
        text = stringResource(id = coreR.string.core_password),
        color = MaterialTheme.appColors.textPrimary,
        style = MaterialTheme.appTypography.labelLarge
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        modifier = modifier.testTag("tf_password"),
        value = passwordTextFieldValue,
        onValueChange = {
            passwordTextFieldValue = it
            onValueChanged(it.text.trim())
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.appColors.textFieldText,
            backgroundColor = MaterialTheme.appColors.textFieldBackground,
            unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
            cursorColor = MaterialTheme.appColors.textFieldText,
        ),
        shape = MaterialTheme.appShapes.textFieldShape,
        placeholder = {
            Text(
                modifier = Modifier.testTag("txt_password_placeholder"),
                text = stringResource(id = R.string.auth_enter_password),
                color = MaterialTheme.appColors.textFieldHint,
                style = MaterialTheme.appTypography.bodyMedium
            )
        },
        trailingIcon = {
            PasswordVisibilityIcon(
                isPasswordVisible = isPasswordVisible,
                onClick = { isPasswordVisible = !isPasswordVisible }
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardActions = KeyboardActions {
            focusManager.clearFocus()
            onPressDone()
        },
        isError = isError,
        textStyle = MaterialTheme.appTypography.bodyMedium,
        singleLine = true,
    )
    if (isError) {
        Text(
            modifier = Modifier
                .testTag("txt_password_error")
                .fillMaxWidth()
                .padding(top = 4.dp),
            text = stringResource(id = R.string.auth_error_empty_password),
            style = MaterialTheme.appTypography.bodySmall,
            color = MaterialTheme.appColors.error,
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "NEXUS_5_Light", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_5_Dark", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = SignInUIState(),
            uiMessage = null,
            onEvent = {},
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "NEXUS_5_Light", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_5_Dark", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignInUsingBrowserScreenPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = SignInUIState().copy(
                isBrowserLoginEnabled = true,
            ),
            uiMessage = null,
            onEvent = {},
        )
    }
}

@Preview(name = "NEXUS_9_Light", device = Devices.NEXUS_9, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_9_Night", device = Devices.NEXUS_9, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenTabletPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Expanded, WindowType.Expanded),
            state = SignInUIState().copy(
                isSocialAuthEnabled = true,
                isFacebookAuthEnabled = true,
                isGoogleAuthEnabled = true,
                isMicrosoftAuthEnabled = true,
            ),
            uiMessage = null,
            onEvent = {},
        )
    }
}
