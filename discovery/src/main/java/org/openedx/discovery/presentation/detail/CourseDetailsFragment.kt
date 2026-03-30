package org.openedx.discovery.presentation.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.domain.model.Media
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.utils.EmailUtil
import org.openedx.discovery.R
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryRouter
import org.openedx.discovery.presentation.ui.ImageHeader
import org.openedx.discovery.presentation.ui.WarningLabel
import org.openedx.foundation.extension.applyDarkModeIfEnabled
import org.openedx.foundation.extension.isEmailValid
import org.openedx.foundation.extension.toImageLink
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import java.nio.charset.StandardCharsets
import java.util.Date
import org.openedx.core.R as CoreR

class CourseDetailsFragment : Fragment() {

    private val viewModel by viewModel<CourseDetailsViewModel> {
        parametersOf(requireArguments().getString(ARG_COURSE_ID, ""))
    }
    private val router by inject<DiscoveryRouter>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()

                val uiState by viewModel.uiState.observeAsState()
                val uiMessage by viewModel.uiMessage.observeAsState()

                val colorBackgroundValue = MaterialTheme.appColors.background.value
                val colorTextValue = MaterialTheme.appColors.textPrimary.value

                CourseDetailsScreen(
                    windowSize = windowSize,
                    uiState = uiState!!,
                    uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl,
                    htmlBody = viewModel.getCourseAboutBody(
                        colorBackgroundValue,
                        colorTextValue
                    ),
                    hasInternetConnection = viewModel.hasInternetConnection,
                    isUserLoggedIn = viewModel.isUserLoggedIn,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onReloadClick = {
                        viewModel.getCourseDetail()
                    },
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    },
                    onButtonClick = {
                        val currentState = uiState
                        if (currentState is CourseDetailsUIState.CourseData) {
                            when {
                                (!currentState.isUserLoggedIn) -> {
                                    val dialog = AuthorizationDialogFragment.newInstance(
                                        viewModel.courseId
                                    )
                                    dialog.show(
                                        requireActivity().supportFragmentManager,
                                        AuthorizationDialogFragment::class.simpleName
                                    )
                                }

                                currentState.course.isEnrolled == true -> {
                                    router.navigateToCourseOutline(
                                        requireActivity().supportFragmentManager,
                                        currentState.course.courseId.orEmpty(),
                                        currentState.course.name.orEmpty(),
                                    )
                                }

                                else -> {
                                    viewModel.enrollInACourse(
                                        currentState.course.courseId.orEmpty(),
                                        currentState.course.name.orEmpty()
                                    )
                                }
                            }
                        }
                    },
                    onWishlistClick = {
                        viewModel.toggleWishlist()
                    },
                    onRegisterClick = {
                        router.navigateToSignUp(parentFragmentManager, viewModel.courseId, null)
                    },
                    onSignInClick = {
                        router.navigateToSignIn(parentFragmentManager, viewModel.courseId, null)
                    },
                )
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        fun newInstance(courseId: String): CourseDetailsFragment {
            val fragment = CourseDetailsFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId
            )
            return fragment
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CourseDetailsScreen(
    windowSize: WindowSize,
    uiState: CourseDetailsUIState,
    uiMessage: UIMessage?,
    apiHostUrl: String,
    htmlBody: String,
    hasInternetConnection: Boolean,
    isUserLoggedIn: Boolean,
    isRegistrationEnabled: Boolean,
    onReloadClick: () -> Unit,
    onBackClick: () -> Unit,
    onButtonClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val configuration = LocalConfiguration.current

    val isInternetConnectionShown = rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .semantics {
                testTagsAsResourceId = true
            },
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background,
        bottomBar = {
            if (!isUserLoggedIn) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)) {
                    AuthButtonsPanel(
                        onRegisterClick = onRegisterClick,
                        onSignInClick = onSignInClick,
                        showRegisterButton = isRegistrationEnabled
                    )
                }
            }
        }
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = if (configuration.orientation == ORIENTATION_LANDSCAPE) {
                        Modifier.widthIn(Dp.Unspecified, 650.dp)
                    } else {
                        Modifier.widthIn(Dp.Unspecified, 560.dp)
                    },
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .statusBarsInset()
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                screenWidth
            ) {
                Toolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f),
                    label = stringResource(id = R.string.discovery_course_details),
                    canShowBackBtn = true,
                    onBackClick = onBackClick
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.appColors.background),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (uiState) {
                        is CourseDetailsUIState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                            }
                        }

                        is CourseDetailsUIState.CourseData -> {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                CourseDetailNativeContent(
                                    windowSize = windowSize,
                                    apiHostUrl = apiHostUrl,
                                    hasInternetConnection = hasInternetConnection,
                                    isInternetConnectionShown = isInternetConnectionShown,
                                    course = uiState.course,
                                    htmlBody = htmlBody,
                                    isWishlisted = uiState.isWishlisted,
                                    curriculum = uiState.curriculum,
                                    instructors = uiState.instructors,
                                    reviews = uiState.reviews,
                                    onButtonClick = {
                                        onButtonClick()
                                    },
                                    onWishlistClick = {
                                        onWishlistClick()
                                    }
                                )
                            }
                        }
                    }
                    if (!isInternetConnectionShown.value && !hasInternetConnection) {
                        OfflineModeDialog(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            onDismissCLick = {
                                isInternetConnectionShown.value = true
                            },
                            onReloadClick = {
                                isInternetConnectionShown.value = true
                                onReloadClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseDetailNativeContent(
    windowSize: WindowSize,
    apiHostUrl: String,
    course: Course,
    hasInternetConnection: Boolean,
    isInternetConnectionShown: MutableState<Boolean>,
    htmlBody: String,
    isWishlisted: Boolean,
    curriculum: Map<String, List<String>>,
    instructors: List<org.openedx.discovery.domain.model.Instructor>,
    reviews: List<org.openedx.discovery.domain.model.Review>,
    onButtonClick: () -> Unit,
    onWishlistClick: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val buttonWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.width(230.dp),
                compact = Modifier.fillMaxWidth()
            )
        )
    }

    val contentHorizontalPadding by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = 6.dp,
                compact = 24.dp
            )
        )
    }

    val buttonText = if (course.isEnrolled == true) {
        stringResource(id = R.string.discovery_view_course)
    } else {
        stringResource(id = R.string.discovery_enroll_now)
    }

    Column {
        // 1. Course Banner (Moved to top)
        Box(contentAlignment = Alignment.Center) {
            ImageHeader(
                modifier = Modifier
                    .aspectRatio(ratio = 1.86f)
                    .padding(6.dp),
                apiHostUrl = apiHostUrl,
                courseImage = course.media?.image?.large,
                courseName = course.name.orEmpty()
            )
            androidx.compose.material.IconButton(
                onClick = onWishlistClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (!course.media?.courseVideo?.uri.isNullOrEmpty()) {
                IconButton(
                    modifier = Modifier.testTag("ib_play_video"),
                    onClick = {
                        uriHandler.openUri(course.media?.courseVideo?.uri!!)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        painter = painterResource(R.drawable.discovery_ic_play),
                        contentDescription = stringResource(id = R.string.discovery_accessibility_play_video),
                        tint = Color.LightGray
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = contentHorizontalPadding)
        ) {
            Spacer(Modifier.height(16.dp))

            // 2. Course Title
            Text(
                modifier = Modifier.testTag("txt_course_name"),
                text = course.name.orEmpty(),
                style = MaterialTheme.appTypography.titleLarge,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))

            // 3. Course Short Description
            Text(
                modifier = Modifier.testTag("txt_course_short_description"),
                text = course.shortDescription.orEmpty(),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textPrimaryVariant
            )
            Spacer(Modifier.height(16.dp))

            // 4. Info Row (Reviews, efforts, students)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.rateStars,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${course.rating?.toDoubleOrNull() ?: 0.0} (${course.noOfReviews ?: 0})",
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = course.effort.orEmpty(),
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${course.enrollments ?: 0} students",
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
            }

            val enrollmentEnd = course.enrollmentEnd
            if (!hasInternetConnection) {
                isInternetConnectionShown.value = true
                Spacer(Modifier.height(16.dp))
                NoInternetLabel()
            } else if (enrollmentEnd != null && Date() > enrollmentEnd) {
                Spacer(Modifier.height(16.dp))
                EnrollOverLabel()
            }

            Spacer(Modifier.height(24.dp))

            // Enrollment Button
            if (!(enrollmentEnd != null && Date() > enrollmentEnd)) {
                OpenEdXButton(
                    modifier = buttonWidth,
                    text = buttonText,
                    onClick = onButtonClick
                )
            }

            Spacer(Modifier.height(24.dp))

            var selectedTab by rememberSaveable { mutableStateOf(0) }
            val tabs = listOf("Overview", "Curriculum", "Instructor", "Reviews")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    Column(
                        modifier = Modifier
                            .clickable { selectedTab = index }
                            .width(IntrinsicSize.Min), // ✅ FIX
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.appTypography.titleSmall,
                            color = if (selectedTab == index)
                                MaterialTheme.appColors.primary
                            else
                                MaterialTheme.appColors.textSecondary
                        )

                        Spacer(Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth() // now matches text width
                                .height(2.dp)
                                .background(
                                    if (selectedTab == index)
                                        MaterialTheme.appColors.primary
                                    else
                                        Color.Transparent
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Overview - removed string "Course Details"
                    var webViewAlpha by remember { mutableFloatStateOf(0f) }
                    if (webViewAlpha == 0f) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().alpha(webViewAlpha),
                        color = MaterialTheme.appColors.background
                    ) {
                        CourseDescription(
                            modifier = Modifier.padding(vertical = 8.dp),
                            apiHostUrl = apiHostUrl,
                            body = htmlBody,
                            onWebPageLoaded = { webViewAlpha = 1f }
                        )
                    }
                }

                1 -> {
                    Text(
                        text = "Course Curriculum",
                        style = MaterialTheme.appTypography.titleMedium,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    if (curriculum.isEmpty()) {
                        Text(
                            text = "No content available",
                            style = MaterialTheme.appTypography.bodySmall,
                            color = MaterialTheme.appColors.textSecondary
                        )
                    } else {
                        curriculum.forEach { (section, items) ->
                            Text(
                                text = section,
                                style = MaterialTheme.appTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.appColors.textDark
                            )
                            Spacer(Modifier.height(8.dp))
                            items.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(MaterialTheme.appColors.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            style = MaterialTheme.appTypography.labelSmall,
                                            color = MaterialTheme.appColors.primary
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = item,
                                        style = MaterialTheme.appTypography.bodyMedium,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                2 -> {
                    Text(
                        text = "Instructors",
                        style = MaterialTheme.appTypography.titleMedium,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    if (instructors.isEmpty()) {
                        Text(
                            text = "No instructor info available",
                            style = MaterialTheme.appTypography.bodySmall,
                            color = MaterialTheme.appColors.textSecondary
                        )
                    } else {
                        instructors.forEach { instructor ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(instructor.profilePicture.toImageLink(apiHostUrl))
                                        .error(CoreR.drawable.core_ic_default_profile_picture)
                                        .placeholder(CoreR.drawable.core_ic_default_profile_picture)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = instructor.name,
                                        style = MaterialTheme.appTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.appColors.textDark
                                    )
                                    if (instructor.designation.isNotEmpty()) {
                                        Text(
                                            text = instructor.designation,
                                            style = MaterialTheme.appTypography.labelMedium,
                                            color = MaterialTheme.appColors.primary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = instructor.bio,
                                        style = MaterialTheme.appTypography.bodySmall,
                                        color = MaterialTheme.appColors.textPrimary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
//                    Text(
//                        text = "Reviews",
//                        style = MaterialTheme.appTypography.titleMedium,
//                        color = MaterialTheme.appColors.textPrimary
//                    )
//                    Spacer(Modifier.height(8.dp))
                    if (reviews.isEmpty()) {
                        Text(
                            text = "No content",
                            style = MaterialTheme.appTypography.bodySmall,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.appColors.surface,
                            shape = MaterialTheme.appShapes.cardShape,
                            elevation = 0.dp,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = String.format("%.1f", course.rating?.toDoubleOrNull() ?: 0.0),
                                    style = MaterialTheme.appTypography.titleLarge,
                                    color = MaterialTheme.appColors.textDark
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.appColors.rateStars
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${course.noOfReviews ?: 0} reviews",
                                    style = MaterialTheme.appTypography.labelSmall,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        reviews.forEach { review ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                color = MaterialTheme.appColors.textFieldBackground,
                                shape = MaterialTheme.appShapes.cardShape
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {

                                    // 🔹 Name + Date (Top Row)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = review.name,
                                            style = MaterialTheme.appTypography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.appColors.textDark
                                        )

                                        Text(
                                            text = review.submittedAt,
                                            style = MaterialTheme.appTypography.labelSmall,
                                            color = MaterialTheme.appColors.textSecondary
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    // 🔹 Stars BELOW name (Left aligned)
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = if (index < review.rating)
                                                    MaterialTheme.appColors.rateStars
                                                else
                                                    Color.LightGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    // 🔹 Comment
                                    Text(
                                        text = review.comment,
                                        style = MaterialTheme.appTypography.bodySmall,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}


@Composable
private fun EnrollOverLabel() {
    WarningLabel(
        painter = rememberVectorPainter(Icons.Outlined.Report),
        text = stringResource(id = R.string.discovery_you_cant_enroll)
    )
}

@Composable
private fun NoInternetLabel() {
    WarningLabel(
        painter = painterResource(id = CoreR.drawable.core_ic_offline),
        text = stringResource(id = R.string.discovery_no_internet_label)
    )
}

@Composable
@SuppressLint("SetJavaScriptEnabled")
private fun CourseDescription(
    modifier: Modifier,
    apiHostUrl: String,
    body: String,
    onWebPageLoaded: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    AndroidView(modifier = Modifier.then(modifier), factory = {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    onWebPageLoaded()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val clickUrl = request?.url?.toString() ?: ""
                    return if (clickUrl.isNotEmpty() && clickUrl.startsWith("http")) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl))
                        )
                        true
                    } else if (clickUrl.startsWith("mailto:")) {
                        val email = clickUrl.replace("mailto:", "")
                        if (email.isEmailValid()) {
                            EmailUtil.sendEmailIntent(context, email, "", "")
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
            }
            with(settings) {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                builtInZoomControls = false
                setSupportZoom(true)
                loadsImagesAutomatically = true
                domStorageEnabled = true
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            loadDataWithBaseURL(
                apiHostUrl,
                body,
                "text/html",
                StandardCharsets.UTF_8.name(),
                null
            )
            applyDarkModeIfEnabled(isDarkTheme)
        }
    })
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CourseDetailNativeContentPreview() {
    OpenEdXTheme {
        CourseDetailsScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = CourseDetailsUIState.CourseData(mockCourse),
            uiMessage = null,
            apiHostUrl = "http://localhost:8000",
            hasInternetConnection = false,
            isUserLoggedIn = true,
            isRegistrationEnabled = true,
            htmlBody = "<b>Preview text</b>",
            onReloadClick = {},
            onBackClick = {},
            onButtonClick = {},
            onWishlistClick = {},
            onRegisterClick = {},
            onSignInClick = {},
        )
    }
}

private val mockCourse = Course(
    id = "id",
    blocksUrl = "blocksUrl",
    courseId = "courseId",
    effort = "effort",
    enrollmentStart = null,
    enrollmentEnd = null,
    hidden = false,
    invitationOnly = false,
    media = Media(),
    mobileAvailable = true,
    name = "Test course",
    number = "number",
    org = "EdX",
    pacing = "pacing",
    shortDescription = "shortDescription",
    start = "start",
    end = "end",
    startDisplay = "startDisplay",
    startType = "startType",
    overview = "",
    isEnrolled = false,
    rating = "0",
    noOfReviews = "0",
    enrollments = "0",
    isWishlisted = false,
    instructorName = "Mahendra",
    category = "Art",
    level = "Beginner"
)
