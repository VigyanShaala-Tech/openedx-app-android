package org.openedx.auth.presentation.signup.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.auth.R
import org.openedx.auth.data.model.AuthType
import org.openedx.auth.presentation.signup.VsSignUpUIState
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.core.R as coreR

@Composable
fun VsSignUpView(
    windowSize: WindowSize,
    uiState: VsSignUpUIState,
    uiMessage: UIMessage?,
    onBackClick: () -> Unit,
    onRegisterClick: (String, String, String, String) -> Unit,
    onSocialRegisterClick: (AuthType) -> Unit,
    onSignInClick: () -> Unit,
    onValidationError: (String) -> Unit,
    onDialogOkClick: () -> Unit = {},
) {
    val scaffoldState = rememberScaffoldState()
    val uriHandler = LocalUriHandler.current

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf("student") }
    var isAgreed by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var fullNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.socialAuth) {
        uiState.socialAuth?.let {
            if (!it.name.isNullOrEmpty()) fullName = it.name!!
            if (!it.email.isNullOrEmpty()) email = it.email!!
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.appColors.primary) // Brand Green background
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                IconButton(
                    onClick = onBackClick,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.appTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238),
                    fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join VigyanShaala and start learning today!",
                style = MaterialTheme.appTypography.bodyMedium.copy(
                    color = Color(0xFF78909C),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Google Sign Up Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onSocialRegisterClick(AuthType.GOOGLE) },
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
                        text = "Sign up with Google",
                        style = MaterialTheme.appTypography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF455A64)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFECEFF1))
                Text(
                    " or ",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFB0BEC5),
                    fontSize = 14.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFECEFF1))
            }
            Spacer(modifier = Modifier.height(24.dp))

            VsSignUpInputField(
                label = "Full Name",
                value = fullName,
                onValueChange = { 
                    fullName = it
                    fullNameError = null
                },
                placeholder = "Kalpna Chawla",
                isRequired = true,
                errorText = fullNameError,
                imeAction = ImeAction.Next
            )

            VsSignUpInputField(
                label = "Email",
                value = email,
                onValueChange = { 
                    email = it
                    emailError = null
                },
                placeholder = "kalpna.chawla@example.com",
                isRequired = true,
                errorText = emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            VsSignUpInputField(
                label = "Password",
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = null
                },
                placeholder = "Create password",
                isRequired = true,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                errorText = passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )

            VsSignUpInputField(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    confirmPasswordError = null
                },
                placeholder = "Confirm password",
                isRequired = true,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                errorText = confirmPasswordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )

            Text(
                text = buildAnnotatedString {
                    append("I am a ")
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                style = MaterialTheme.appTypography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF455A64)
                ),
                modifier = Modifier.padding(top = 24.dp)
            )
            Row(modifier = Modifier.padding(top = 12.dp)) {
                VsRoleButton(
                    text = "Student",
                    isSelected = selectedRole == "student",
                    onClick = { selectedRole = "student" },
                    modifier = Modifier.weight(1f),
                    selectedColor = MaterialTheme.appColors.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                VsRoleButton(
                    text = "Mentor",
                    isSelected = selectedRole == "mentor",
                    onClick = { selectedRole = "mentor" },
                    modifier = Modifier.weight(1f),
                    selectedColor = MaterialTheme.appColors.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.dp, if (isAgreed) MaterialTheme.appColors.primary else Color(0xFFB0BEC5), CircleShape)
                        .clip(CircleShape)
                        .clickable { isAgreed = !isAgreed }
                        .padding(4.dp)
                ) {
                    if (isAgreed) {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.primary, CircleShape))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                val annotatedText = buildAnnotatedString {
                    append("I agree to the VigyanShaala International ")
                    withStyle(style = SpanStyle(color = MaterialTheme.appColors.primary, fontWeight = FontWeight.Bold)) {
                        append("Terms of Service")
                    }
                }
                Text(
                    text = annotatedText,
                    style = MaterialTheme.appTypography.bodySmall.copy(color = Color(0xFF78909C)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { 
                            if (uiState.tosUrl.isNotBlank()) {
                                uriHandler.openUri(uiState.tosUrl)
                            } else {
                                uriHandler.openUri("https://vigyanshaala.com/terms-of-service/")
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    fullNameError = if (fullName.isBlank()) "Please enter your full name" else null
                    emailError = when {
                        email.isBlank() -> "Please enter your email"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email"
                        else -> null
                    }
                    passwordError = if (password.length < 8) "Password must be at least 8 characters" else null
                    confirmPasswordError = if (password != confirmPassword) "Passwords do not match" else null

                    if (fullNameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
                        if (isAgreed) {
                            onRegisterClick(email, fullName, password, selectedRole)
                        } else {
                            onValidationError("Please agree to the Terms of Service")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFC5E1A5), // Light green
                    disabledBackgroundColor = Color(0xFFE0E0E0)
                ),
                enabled = !uiState.isButtonLoading
            ) {
                if (uiState.isButtonLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Create Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Already have an account? ",
                    color = Color(0xFF78909C),
                    fontSize = 16.sp
                )
                Text(
                    "Sign in",
                    color = MaterialTheme.appColors.primary,
                    modifier = Modifier.clickable { onSignInClick() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (uiState.showRegisterSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Verify your email",
                    style = MaterialTheme.appTypography.titleMedium,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Please verify your email to login.",
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = { onDialogOkClick() }) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.appTypography.labelLarge,
                        color = MaterialTheme.appColors.primary
                    )
                }
            }
        )
    }
}

@Composable
fun VsSignUpInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isRequired: Boolean = false,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
                }
            },
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
                .height(56.dp),
            placeholder = { 
                Text(
                    text = placeholder, 
                    color = Color(0xFFB0BEC5),
                    fontSize = 15.sp
                ) 
            },
            trailingIcon = if (isPassword) {
                {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { onPasswordToggle?.invoke() }) {
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
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
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
fun VsRoleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.1f) else Color(0xFFF1F4F6),
        border = if (isSelected) BorderStroke(1.dp, selectedColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) selectedColor else Color(0xFF455A64),
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 16.sp
            )
        }
    }
}
