package org.openedx.course.presentation.registration

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.core.R as coreR

@Composable
fun CourseRegistrationScreen(
    windowSize: WindowSize,
    uiState: CourseRegistrationUIState,
    uiMessage: UIMessage?,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White)
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
                        tint = MaterialTheme.appColors.textDark
                    )
                }
                Image(
                    painter = painterResource(id = coreR.drawable.core_ic_logo),
                    contentDescription = "Vigyan Shaala",
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.Center)
                        .height(60.dp)
                        .padding(top = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        },
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is CourseRegistrationUIState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.appColors.primary
                    )
                }
                is CourseRegistrationUIState.CourseData -> {
                    CourseRegistrationContent(
                        uiState = uiState,
                        onNextClick = onNextClick,
                        onPreviousClick = onPreviousClick
                    )
                }
                is CourseRegistrationUIState.Error -> {
                    // TODO: Implement error state
                }
            }
        }
    }
}

@Composable
fun CourseRegistrationContent(
    uiState: CourseRegistrationUIState.CourseData,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val category = uiState.enrollmentForm.categories.getOrNull(uiState.currentStep - 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Registration Form",
            style = MaterialTheme.appTypography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Step Indicator
        RegistrationStepper(
            currentStep = uiState.currentStep,
            totalSteps = uiState.enrollmentForm.categories.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.currentStep > 1) {
            OutlinedButton(
                onClick = onPreviousClick,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Back", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        category?.let {
            if (it.description.isNotEmpty()) {
                Text(
                    text = it.description,
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            it.fields.filter { it.visible }.forEach { field ->
                RegistrationField(field = field)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OpenEdXButton(
            text = if (uiState.currentStep == uiState.enrollmentForm.categories.size) "Submit" else "Next",
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            backgroundColor = MaterialTheme.appColors.primary
        )

        if (uiState.currentStep == 1 && uiState.enrollmentForm.infoSections.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            HtmlInfoSection(html = uiState.enrollmentForm.infoSections)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RegistrationField(field: org.openedx.core.domain.model.EnrollmentRegistrationField) {
    Column {
        Text(
            text = field.label + if (field.required) " *" else "",
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.appColors.textDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        when (field.type) {
            "text", "email", "textarea" -> {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = field.placeholder) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            "select" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = field.placeholder.ifEmpty { "Select " + field.label }, color = Color.Gray)
                    Icon(
                        painter = painterResource(id = org.openedx.course.R.drawable.course_ic_arrow_down),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    )
                }
            }
            "radio" -> {
                // Simplified radio rendering
                Column {
                    // Logic to render options if any
                }
            }
        }
    }
}

@Composable
fun HtmlInfoSection(html: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            loadData(html, "text/html", "UTF-8")
        }
    }, modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp))
}

@Composable
fun RegistrationStepper(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            StepCircle(
                step = i,
                isCompleted = i < currentStep,
                isSelected = i == currentStep
            )
            if (i < totalSteps) {
                Divider(
                    modifier = Modifier.width(60.dp),
                    color = if (i < currentStep) MaterialTheme.appColors.primary else Color.LightGray,
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
fun StepCircle(
    step: Int,
    isCompleted: Boolean,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isCompleted || isSelected) MaterialTheme.appColors.primary else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isCompleted || isSelected) MaterialTheme.appColors.primary else Color.LightGray,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                painter = painterResource(id = coreR.drawable.ic_core_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = step.toString(),
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}
