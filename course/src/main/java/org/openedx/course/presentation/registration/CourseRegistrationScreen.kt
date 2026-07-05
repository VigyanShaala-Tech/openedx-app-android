package org.openedx.course.presentation.registration

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.openedx.core.domain.model.EnrollmentRegistrationField
import org.openedx.core.domain.model.EnrollmentRegistrationOption
import org.openedx.core.ui.HandleUIMessage
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
    answers: Map<String, String>,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onAnswerUpdate: (String, String) -> Unit,
    isNextEnabled: Boolean
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
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
                        tint = Color.Black
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
                Text(
                    text = "Registration Form",
                    style = MaterialTheme.appTypography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        },
        backgroundColor = Color(0xFFF5F5F5) // Light grey background like in image
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
                        answers = answers,
                        onNextClick = onNextClick,
                        onPreviousClick = onPreviousClick,
                        onAnswerUpdate = onAnswerUpdate,
                        isNextEnabled = isNextEnabled
                    )
                }
                is CourseRegistrationUIState.Error -> {
                    // Handled by snackbars
                }
            }
        }
    }
}

@Composable
fun CourseRegistrationContent(
    uiState: CourseRegistrationUIState.CourseData,
    answers: Map<String, String>,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onAnswerUpdate: (String, String) -> Unit,
    isNextEnabled: Boolean
) {
    val scrollState = rememberScrollState()
    val category = uiState.enrollmentForm.categories.getOrNull(uiState.currentStep - 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White) // White card-like background
            .padding(horizontal = 24.dp)
    ) {
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
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBDBDBD)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF424242))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.appTypography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        category?.let {
            if (it.description.isNotEmpty()) {
                Text(
                    text = it.description,
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            it.fields.filter { it.visible }.forEach { field ->
                RegistrationFieldItem(
                    field = field,
                    currentValue = answers[field.name] ?: "",
                    onValueChange = { newValue -> onAnswerUpdate(field.name, newValue) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNextClick,
            enabled = isNextEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.dp,
                    color = if (isNextEnabled) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color(0xFF4CAF50),
                disabledBackgroundColor = Color.White,
                disabledContentColor = Color(0xFFBDBDBD)
            ),
            elevation = null
        ) {
            Text(
                text = if (uiState.currentStep == uiState.enrollmentForm.categories.size) "Submit" else "Next",
                style = MaterialTheme.appTypography.labelLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
        }

        if (uiState.currentStep == 1 && uiState.enrollmentForm.infoSections.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            HtmlInfoSection(html = uiState.enrollmentForm.infoSections)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RegistrationFieldItem(
    field: EnrollmentRegistrationField,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    val options = remember(field.options) {
        parseOptions(field.options)
    }

    var showDialog by remember { mutableStateOf(false) }

    when (field.type) {
        "text", "email", "textarea" -> {
            VsRegistrationTextField(
                label = field.label,
                value = currentValue,
                onValueChange = onValueChange,
                placeholder = field.placeholder,
                isRequired = field.required,
                helperText = field.helper.takeIf { it.isNotEmpty() }
            )
        }
        "select" -> {
            val selectedOption = options.find { it.value == currentValue }
            VsRegistrationSelectField(
                label = field.label,
                value = selectedOption?.label ?: "",
                onClick = { showDialog = true },
                placeholder = field.placeholder,
                isRequired = field.required,
                helperText = field.helper.takeIf { it.isNotEmpty() }
            )
        }
        "radio" -> {
            VsRegistrationRadioField(
                label = field.label,
                options = options,
                selectedValue = currentValue,
                onValueChange = onValueChange,
                isRequired = field.required
            )
        }
    }

    if (showDialog) {
        SelectionDialog(
            title = field.label,
            options = options,
            onDismiss = { showDialog = false },
            onSelect = { 
                onValueChange(it.value)
                showDialog = false
            }
        )
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<EnrollmentRegistrationOption>,
    onDismiss: () -> Unit,
    onSelect: (EnrollmentRegistrationOption) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredOptions = options.filter { it.label.contains(searchQuery, ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, style = MaterialTheme.appTypography.titleMedium, color = Color.Black)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Search...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredOptions) { option ->
                        Text(
                            text = option.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option) }
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = Color.Black
                        )
                        Divider(color = Color.LightGray)
                    }
                }
            }
        }
    }
}

private fun parseOptions(options: Any?): List<EnrollmentRegistrationOption> {
    if (options == null) return emptyList()
    
    if (options is List<*>) {
        return options.filterIsInstance<EnrollmentRegistrationOption>()
    }
    
    return try {
        val gson = Gson()
        val json = gson.toJson(options)
        val type = object : TypeToken<List<EnrollmentRegistrationOption>>() {}.type
        gson.fromJson(json, type)
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun HtmlInfoSection(html: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    }, modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp))
}
