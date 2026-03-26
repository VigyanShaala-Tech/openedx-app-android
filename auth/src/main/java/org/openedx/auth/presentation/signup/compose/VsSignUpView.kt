package org.openedx.auth.presentation.signup.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.auth.R
import org.openedx.auth.presentation.signup.VsSignUpUIState
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
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
    onRegisterClick: (String, String, String, String, String, String?) -> Unit,
    onSignInClick: () -> Unit,
    onSendOtpClick: (String) -> Unit,
    onVerifyOtpClick: (String, String) -> Unit,
    onValidationError: (String) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }
    var isAgreed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Image(
                    painter = painterResource(id = coreR.drawable.core_green_gradient_rect),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(top = 24.dp, start = 8.dp)
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
                        .align(Alignment.Center)
                        .height(60.dp),
                    contentScale = ContentScale.Fit
                )
            }
        },
        backgroundColor = MaterialTheme.appColors.background
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
                    color = MaterialTheme.appColors.textPrimary,
                    fontSize = 28.sp
                )
            )
            Text(
                text = "Join VigyanShaala and start learning today!",
                style = MaterialTheme.appTypography.bodyMedium.copy(color = MaterialTheme.appColors.textSecondary)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign In Button
            OutlinedButton(
                onClick = { /* Handle Google Sign In */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = MaterialTheme.appShapes.buttonShape,
                border = BorderStroke(1.dp, MaterialTheme.appColors.cardViewBorder)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.auth_ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign up with Google", color = MaterialTheme.appColors.textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.appColors.cardViewBorder)
                Text(" or ", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.appColors.textSecondary)
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.appColors.cardViewBorder)
            }
            Spacer(modifier = Modifier.height(16.dp))

            VsSignUpInputField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Enter your full name",
                isRequired = true,
                helperText = "This name will be used on any certificates you earn."
            )

            VsSignUpInputField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                isRequired = true,
                helperText = "This is what you will use to login."
            )

            // Mobile Number with OTP verification logic
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Mobile Number (Optional)",
                    style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.isOtpVerified) {
                    // Verified State UI
                    TextField(
                        value = mobileNumber,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Outlined.Smartphone, contentDescription = null, tint = MaterialTheme.appColors.textPrimary) },
                        trailingIcon = { 
                            Icon(
                                imageVector = Icons.Default.CheckCircle, 
                                contentDescription = "Verified", 
                                tint = MaterialTheme.appColors.primary,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        shape = MaterialTheme.appShapes.textFieldShape,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = MaterialTheme.appColors.textFieldBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                } else {
                    // Normal / Sent state UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 2.dp,
                                    color = if (uiState.isOtpSent) MaterialTheme.appColors.primary else Color.Transparent,
                                    shape = MaterialTheme.appShapes.textFieldShape
                                ),
                            placeholder = { Text("+91 Enter mobile number", color = MaterialTheme.appColors.textSecondary) },
                            leadingIcon = { Icon(Icons.Outlined.Smartphone, contentDescription = null, tint = MaterialTheme.appColors.textPrimary) },
                            shape = MaterialTheme.appShapes.textFieldShape,
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = MaterialTheme.appColors.textFieldBackground,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        
                        AnimatedVisibility(
                            visible = mobileNumber.isNotEmpty() || uiState.isOtpSent,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { onSendOtpClick(mobileNumber) },
                                    modifier = Modifier
                                        .height(56.dp)
                                        .widthIn(min = 100.dp),
                                    shape = MaterialTheme.appShapes.buttonShape,
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.appColors.background,
                                        contentColor = MaterialTheme.appColors.textPrimary
                                    ),
                                    elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.appColors.cardViewBorder),
                                    enabled = !uiState.isOtpLoading
                                ) {
                                    if (uiState.isOtpLoading && !uiState.isOtpSent) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.appColors.textPrimary)
                                    } else {
                                        Text(
                                            text = if (uiState.isOtpSent) "Sent" else "Send OTP",
                                            fontSize = 14.sp,
                                            color = if (uiState.isOtpSent) MaterialTheme.appColors.textSecondary else MaterialTheme.appColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.isOtpSent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Enter OTP", color = MaterialTheme.appColors.textSecondary)
                                    }
                                },
                                shape = MaterialTheme.appShapes.textFieldShape,
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = MaterialTheme.appColors.textFieldBackground,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { onVerifyOtpClick(mobileNumber, otpCode) },
                                modifier = Modifier
                                    .height(56.dp)
                                    .widthIn(min = 100.dp),
                                shape = MaterialTheme.appShapes.buttonShape,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.appColors.primary,
                                    contentColor = Color.White
                                ),
                                enabled = otpCode.length >= 4 && !uiState.isOtpLoading
                            ) {
                                if (uiState.isOtpLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Text("Verify", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "Verify your mobile to enable OTP login.",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            VsSignUpInputField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Create a password",
                isRequired = true,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                helperText = "Password must be at least 8 characters."
            )

            VsSignUpInputField(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm your password",
                isRequired = true,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Text(
                text = buildAnnotatedString {
                    append("I am a ")
                    withStyle(style = SpanStyle(color = MaterialTheme.appColors.error)) { append("*") }
                },
                style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(modifier = Modifier.padding(top = 8.dp)) {
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
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.appColors.primary)
                )
                Text(
                    text = buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(style = SpanStyle(color = MaterialTheme.appColors.primary)) { append("Terms of Service") }
                        append(" and ")
                        withStyle(style = SpanStyle(color = MaterialTheme.appColors.primary)) { append("Privacy Policy") }
                    },
                    style = MaterialTheme.appTypography.bodySmall,
                    modifier = Modifier.clickable { isAgreed = !isAgreed }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val error = vsValidationError(fullName, email, password, confirmPassword, isAgreed)
                    if (error == null) {
                        onRegisterClick(email, fullName, password, mobileNumber, selectedRole, uiState.verificationKey)
                    } else {
                        onValidationError(error)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.appShapes.buttonShape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.appColors.primary,
                    disabledBackgroundColor = MaterialTheme.appColors.inactiveButtonBackground
                ),
                enabled = !uiState.isButtonLoading && isAgreed
            ) {
                if (uiState.isButtonLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = MaterialTheme.appColors.textSecondary)
                Text(
                    "Sign in",
                    color = MaterialTheme.appColors.primary,
                    modifier = Modifier.clickable { onSignInClick() },
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
    helperText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = MaterialTheme.appColors.error)) { append(" *") }
                }
            },
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.appColors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.appColors.textSecondary) },
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword) {
                {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { onPasswordToggle?.invoke() }) {
                        Icon(icon, contentDescription = null)
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
            shape = MaterialTheme.appShapes.textFieldShape,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.appColors.textFieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
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
        shape = MaterialTheme.appShapes.buttonShape,
        color = if (isSelected) selectedColor.copy(alpha = 0.1f) else MaterialTheme.appColors.textFieldBackground,
        border = if (isSelected) BorderStroke(1.dp, selectedColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) selectedColor else MaterialTheme.appColors.textSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

fun vsValidationError(fullName: String, email: String, pass: String, confirmPass: String, agreed: Boolean): String? {
    if (fullName.isBlank()) return "Please enter your full name"
    if (email.isBlank()) return "Please enter your email"
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Please enter a valid email"
    if (pass.length < 8) return "Password must be at least 8 characters"
    if (pass != confirmPass) return "Passwords do not match"
    if (!agreed) return "Please agree to the Terms of Service and Privacy Policy"
    return null
}
