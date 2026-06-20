package org.openedx.auth.presentation.restore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.R
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.auth.R as authR
import org.koin.android.ext.android.inject

class ResetPasswordFragment : Fragment() {

    private val viewModel: ResetPasswordViewModel by viewModel()
    private val router: AuthRouter by inject()
    private val token: String by lazy {
        requireArguments().getString(ARG_TOKEN, "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.validateToken(token)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()

                val uiState by viewModel.uiState.observeAsState(ResetPasswordUIState.Loading)
                val uiMessage by viewModel.uiMessage.observeAsState()
                val isButtonLoading by viewModel.isButtonLoading.observeAsState(false)

                ResetPasswordScreen(
                    windowSize = windowSize,
                    uiState = uiState,
                    uiMessage = uiMessage,
                    isButtonLoading = isButtonLoading,
                    onBackClick = {
                        if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
                            requireActivity().supportFragmentManager.popBackStackImmediate()
                        } else {
                            router.navigateToSignIn(requireActivity().supportFragmentManager, null, null)
                        }
                    },
                    onResetButtonClick = { p1, p2 ->
                        viewModel.resetPassword(token, p1, p2)
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_TOKEN = "token"
        fun newInstance(token: String): ResetPasswordFragment {
            val fragment = ResetPasswordFragment()
            fragment.arguments = bundleOf(ARG_TOKEN to token)
            return fragment
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ResetPasswordScreen(
    windowSize: WindowSize,
    uiState: ResetPasswordUIState,
    uiMessage: UIMessage?,
    isButtonLoading: Boolean,
    onBackClick: () -> Unit,
    onResetButtonClick: (String, String) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            }
            .fillMaxSize()
            .navigationBarsPadding(),
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->

        val contentPaddings by remember {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier
                        .widthIn(Dp.Unspecified, 420.dp)
                        .padding(top = 32.dp, bottom = 40.dp),
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
                .height(200.dp),
            painter = painterResource(id = R.drawable.core_green_gradient_rect),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )

        HandleUIMessage(
            uiMessage = uiMessage,
            scaffoldState = scaffoldState
        )

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .statusBarsInset(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = authR.string.auth_confirm_reset_password),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.appTypography.titleMedium
                )
                BackBtn(
                    modifier = Modifier.padding(end = 16.dp),
                    tint = Color.White
                ) {
                    onBackClick()
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.appColors.background,
                shape = MaterialTheme.appShapes.screenBackgroundShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .background(MaterialTheme.appColors.background),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (uiState) {
                        ResetPasswordUIState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                            }
                        }

                        ResetPasswordUIState.ValidLink -> {
                            Column(
                                Modifier
                                    .then(contentPaddings)
                                    .displayCutoutForLandscape()
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(id = authR.string.auth_reset_password),
                                    style = MaterialTheme.appTypography.displaySmall,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                                Spacer(Modifier.height(32.dp))
                                PasswordTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(id = authR.string.auth_new_password),
                                    onValueChanged = { password = it },
                                    imeAction = ImeAction.Next
                                )
                                Spacer(Modifier.height(16.dp))
                                PasswordTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(id = authR.string.auth_confirm_new_password),
                                    onValueChanged = { confirmPassword = it },
                                    imeAction = ImeAction.Done,
                                    keyboardActions = {
                                        keyboardController?.hide()
                                        if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                            onResetButtonClick(password, confirmPassword)
                                        }
                                    }
                                )
                                Spacer(Modifier.height(50.dp))
                                if (isButtonLoading) {
                                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                } else {
                                    OpenEdXButton(
                                        modifier = buttonWidth,
                                        text = stringResource(id = authR.string.auth_reset_password),
                                        onClick = {
                                            keyboardController?.hide()
                                            onResetButtonClick(password, confirmPassword)
                                        }
                                    )
                                }
                            }
                        }

                        ResetPasswordUIState.InvalidLink -> {
                            Column(
                                Modifier
                                    .then(contentPaddings)
                                    .displayCutoutForLandscape()
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(80.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = Color(0xFFFFEBEE)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    text = stringResource(id = authR.string.auth_invalid_password_reset_link),
                                    style = MaterialTheme.appTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.appColors.textDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = stringResource(id = authR.string.auth_invalid_password_reset_link_desc),
                                    style = MaterialTheme.appTypography.bodyMedium,
                                    color = MaterialTheme.appColors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(32.dp))
                                Divider(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    color = Color.LightGray,
                                    thickness = 0.5.dp
                                )
                                Spacer(Modifier.height(32.dp))
                                Row(
                                    modifier = Modifier.clickable { onBackClick() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.appColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = authR.string.auth_back_to_sign_in),
                                        style = MaterialTheme.appTypography.labelLarge,
                                        color = MaterialTheme.appColors.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(Modifier.height(24.dp))
                                val annotatedString = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = MaterialTheme.appColors.textPrimary, fontSize = 14.sp)) {
                                        append("Remember your password? ")
                                    }
                                    withStyle(style = SpanStyle(color = MaterialTheme.appColors.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                                        append("Sign In")
                                    }
                                }
                                Text(
                                    modifier = Modifier.clickable { onBackClick() },
                                    text = annotatedString,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        ResetPasswordUIState.Success -> {
                            Column(
                                Modifier
                                    .then(contentPaddings)
                                    .displayCutoutForLandscape()
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier.size(100.dp),
                                    painter = painterResource(id = authR.drawable.auth_ic_email),
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.primary
                                )
                                Spacer(Modifier.height(48.dp))
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = stringResource(id = authR.string.auth_password_reset_success),
                                    style = MaterialTheme.appTypography.titleLarge,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                                Spacer(Modifier.height(48.dp))
                                OpenEdXButton(
                                    modifier = buttonWidth,
                                    text = stringResource(id = R.string.core_sign_in),
                                    onClick = { onBackClick() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    title: String,
    onValueChanged: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: () -> Unit = {},
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        Text(
            text = title,
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onValueChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { keyboardActions() }, onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.appColors.textFieldText,
                backgroundColor = MaterialTheme.appColors.textFieldBackground,
                unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                cursorColor = MaterialTheme.appColors.textFieldText,
            ),
            shape = MaterialTheme.appShapes.textFieldShape
        )
    }
}
