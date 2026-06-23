package org.openedx.auth.presentation.signin.compose

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.auth.R
import org.openedx.auth.data.model.AuthType
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInUIState
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
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
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.appColors.primary) // Brand Green
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            ) {
                IconButton(
                    onClick = { onEvent(AuthEvent.BackClick) },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Image(
                    painter = painterResource(id = coreR.drawable.core_logo_white),
                    contentDescription = "Vigyan Shaala",
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.Center)
                        .height(70.dp)
                        .padding(top = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        },
        backgroundColor = Color.White
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sign in",
                style = MaterialTheme.appTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238),
                    fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome back! Sign in to access your courses.",
                style = MaterialTheme.appTypography.bodyMedium.copy(
                    color = Color(0xFF78909C),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Google Sign In Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onEvent(AuthEvent.SocialSignIn(AuthType.GOOGLE)) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFECEFF1)),
                color = Color(0xFFFAFAFA)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.auth_ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.appTypography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF455A64)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            DividerWithOrLabel()
            Spacer(modifier = Modifier.height(24.dp))

            AuthForm(state, onEvent)

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Don't have an account? ",
                    color = Color(0xFF78909C),
                    fontSize = 16.sp
                )
                Text(
                    "Sign up",
                    color = MaterialTheme.appColors.primary,
                    modifier = Modifier.clickable { onEvent(AuthEvent.RegisterClick) },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun AuthForm(
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
            Spacer(modifier = Modifier.height(24.dp))
            if (isEmailTab) {
                SignInInputField(
                    label = "Email Address",
                    value = login,
                    onValueChange = {
                        login = it
                        isEmailError = false
                    },
                    placeholder = "kalpna.chawla@example.com",
                    leadingIcon = Icons.Default.PersonOutline,
                    errorText = if (isEmailError) stringResource(id = R.string.auth_error_empty_username_email) else null,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(18.dp))
                SignInInputField(
                    label = "Password",
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordError = false
                    },
                    placeholder = "Enter your password",
                    isPassword = true,
                    errorText = if (isPasswordError) stringResource(id = R.string.auth_error_empty_password) else null,
                    imeAction = ImeAction.Done,
                    onImeAction = {
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
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (!state.isBrowserLoginEnabled && isEmailTab) {
                Text(
                    modifier = Modifier
                        .testTag("txt_forgot_password")
                        .noRippleClickable {
                            onEvent(AuthEvent.ForgotPasswordClick)
                        },
                    text = stringResource(id = R.string.auth_forgot_password),
                    color = MaterialTheme.appColors.primary,
                    style = MaterialTheme.appTypography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (state.showProgress) {
            CircularProgressIndicator(color = MaterialTheme.appColors.primary)
        } else {
            OpenEdXButton(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                text = if (isEmailTab) stringResource(id = coreR.string.core_sign_in)
                       else if (!isOtpSent) "Send OTP"
                       else stringResource(id = R.string.auth_verify_and_sign_in),
                backgroundColor = Color(0xFF8BC34A),
                onClick = {
                    keyboardController?.hide()
                    if (isEmailTab) {
                        if (login.isNotEmpty() && password.isNotEmpty()) {
                            onEvent(AuthEvent.SignIn(login = login, password = password))
                        } else {
                            isEmailError = login.isEmpty()
                            isPasswordError = password.isEmpty()
                        }
                    } else {
                        if (!isOtpSent) {
                            if (mobile.isNotEmpty()) {
                                onEvent(AuthEvent.SendOtp(mobile))
                                isOtpSent = true
                            } else {
                                isMobileError = true
                            }
                        } else {
                            if (otp.isNotEmpty()) {
                                onEvent(AuthEvent.VerifyOtp(mobile, otp))
                            } else {
                                isOtpError = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthMethodTabs(
    isEmailSelected: Boolean,
    onSelectEmail: () -> Unit,
    onSelectOtp: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F4F6), shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            ToggleItem(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                text = stringResource(id = R.string.auth_email),
                icon = Icons.Filled.Email,
                isSelected = isEmailSelected,
                onClick = onSelectEmail
            )
            ToggleItem(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                text = "Mobile",
                icon = Icons.Filled.PhoneAndroid,
                isSelected = !isEmailSelected,
                onClick = onSelectOtp
            )
        }
    }
}

@Composable
private fun ToggleItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) Color.White else Color.Transparent
    val tint = if (isSelected) Color(0xFF455A64) else Color(0xFF90A4AE)
    Row(
        modifier = modifier
            .background(bg, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = tint,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun SignInInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isPassword: Boolean = false,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {}
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.appTypography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF455A64)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { isFocused = it.isFocused },
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFB0BEC5),
                    fontSize = 15.sp
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isFocused) MaterialTheme.appColors.primary else Color(0xFFB0BEC5)
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Color(0xFF455A64)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = {
                    focusManager.clearFocus()
                    onImeAction()
                }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF263238),
                backgroundColor = Color(0xFFF1F4F6),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.appColors.primary,
                cursorColor = Color(0xFF263238),
                errorBorderColor = Color.Red
            ),
            isError = errorText != null,
            singleLine = true
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.appTypography.bodySmall,
                color = Color.Red,
                modifier = Modifier.padding(top = 4.dp)
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
    isMobileError: Boolean,
    isOtpError: Boolean,
    secondsLeft: Int,
    canResend: Boolean,
    onResend: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SignInInputField(
            label = "WhatsApp Number",
            value = mobile,
            onValueChange = {
                val raw = it.trim()
                val normalized = if (raw.startsWith("+91")) raw else "+91${raw.removePrefix("+")}"
                onMobileChanged(normalized)
            },
            placeholder = "Enter WhatsApp number",
            leadingIcon = Icons.Default.PhoneAndroid,
            errorText = if (isMobileError) "Please enter a valid mobile number" else null,
            imeAction = ImeAction.Next
        )

        if (isOtpSent) {
            Spacer(modifier = Modifier.height(18.dp))
            SignInInputField(
                label = "OTP",
                value = otp,
                onValueChange = onOtpChanged,
                placeholder = "Enter OTP",
                leadingIcon = Icons.Default.PhonelinkLock,
                errorText = if (isOtpError) "Please enter a valid OTP" else null,
                imeAction = ImeAction.Done
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.auth_otp_sent_to),
                    color = Color(0xFF78909C),
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = Color(0xFFECEFF1))
        Text(
            text = stringResource(id = R.string.auth_or),
            color = Color(0xFFB0BEC5),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp
        )
        Divider(modifier = Modifier.weight(1f), color = Color(0xFFECEFF1))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
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
