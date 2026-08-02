package org.openedx.course.presentation.registration

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.RenderHtmlContent
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
    eligibilityErrors: Map<String, String>,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onAnswerUpdate: (EnrollmentRegistrationField, String) -> Unit,
    onAnswerUpdateByName: (String, String) -> Unit,
    isNextEnabled: Boolean,
    isFieldVisible: (EnrollmentRegistrationField) -> Boolean
) {
    val scaffoldState = rememberScaffoldState()

    BackHandler(enabled = (uiState is CourseRegistrationUIState.CourseData && uiState.currentStep > 1)) {
        onPreviousClick()
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
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
                            .align(Alignment.Center)
                            .height(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Registration Form",
                        style = MaterialTheme.appTypography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            textAlign = TextAlign.Center
                        )
                    )

                    val eligibilityNote = (uiState as? CourseRegistrationUIState.CourseData)?.enrollmentForm?.eligibilityNote
                    if (!eligibilityNote.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            RenderHtmlContent(html = eligibilityNote)
                        }
                    }
                }
            }
        },
        backgroundColor = Color(0xFFF5F5F5)
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
                        eligibilityErrors = eligibilityErrors,
                        onNextClick = onNextClick,
                        onAnswerUpdate = onAnswerUpdate,
                        onAnswerUpdateByName = onAnswerUpdateByName,
                        isNextEnabled = isNextEnabled,
                        isFieldVisible = isFieldVisible
                    )
                }
                is CourseRegistrationUIState.SubmissionSuccess -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = coreR.drawable.ic_core_check),
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Registration Successful!",
                                style = MaterialTheme.appTypography.titleLarge,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onBackClick) {
                                Text(text = "Go Back")
                            }
                        }
                    }
                }
                is CourseRegistrationUIState.Error -> { }
            }
        }
        if (uiState is CourseRegistrationUIState.CourseData && uiState.isSubmitting) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
fun CourseRegistrationContent(
    uiState: CourseRegistrationUIState.CourseData,
    answers: Map<String, String>,
    eligibilityErrors: Map<String, String>,
    onNextClick: () -> Unit,
    onAnswerUpdate: (EnrollmentRegistrationField, String) -> Unit,
    onAnswerUpdateByName: (String, String) -> Unit,
    isNextEnabled: Boolean,
    isFieldVisible: (EnrollmentRegistrationField) -> Boolean
) {
    val scrollState = rememberScrollState()
    val category = uiState.enrollmentForm.categories.getOrNull(uiState.currentStep - 1)

    LaunchedEffect(uiState.currentStep) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        RegistrationStepper(
            currentStep = uiState.currentStep,
            totalSteps = uiState.enrollmentForm.categories.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        category?.let {
            if (it.description.isNotEmpty()) {
                Text(
                    text = it.description,
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            it.fields.filter { isFieldVisible(it) }.forEach { field ->
                RegistrationFieldItem(
                    field = field,
                    currentValue = answers[field.name] ?: "",
                    errorText = eligibilityErrors[field.name],
                    answers = answers,
                    onValueChange = { newValue -> onAnswerUpdate(field, newValue) },
                    onAnswerUpdateByName = onAnswerUpdateByName
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
    errorText: String?,
    answers: Map<String, String>,
    onValueChange: (String) -> Unit,
    onAnswerUpdateByName: (String, String) -> Unit
) {
    val options = remember(field.options, answers[field.dependsOn]) {
        parseOptions(field, answers)
    }

    var showDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onValueChange(it.toString())
        }
    }

    val isOtherSelected = remember(currentValue, options) {
        val selectedValues = currentValue.split("|").filter { it.isNotEmpty() }
        selectedValues.any { valId ->
            val label = options.find { it.value == valId }?.label ?: valId
            valId.lowercase() == "other" || valId.lowercase() == "others" ||
                    valId.lowercase() == "__other__" ||
                    label.lowercase() == "other" || label.lowercase() == "others"
        }
    }

    Column {
        when (field.type) {
            "text", "email", "textarea", "number" -> {
                VsRegistrationTextField(
                    label = field.label,
                    value = currentValue,
                    onValueChange = onValueChange,
                    placeholder = field.placeholder,
                    isRequired = field.required,
                    isTextArea = field.type == "textarea",
                    helperText = field.helper.takeIf { it.isNotEmpty() },
                    errorText = errorText,
                    enabled = field.isEditable,
                    keyboardType = when (field.type) {
                        "number" -> KeyboardType.Number
                        "email" -> KeyboardType.Email
                        else -> KeyboardType.Text
                    }
                )
            }
            "select", "multi-select" -> {
                val isMultiSelect = field.type == "multi-select"
                val selectedLabels = if (isMultiSelect) {
                    currentValue.split("|").filter { it.isNotEmpty() }.map { valId ->
                        options.find { it.value == valId }?.label ?: valId
                    }.joinToString(", ")
                } else {
                    options.find { it.value == currentValue }?.label ?: currentValue
                }
                
                VsRegistrationSelectField(
                    label = field.label,
                    value = selectedLabels,
                    onClick = { showDialog = true },
                    placeholder = field.placeholder,
                    isRequired = field.required,
                    helperText = field.helper.takeIf { it.isNotEmpty() },
                    enabled = field.isEditable
                )
                if (errorText != null) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            "radio" -> {
                VsRegistrationRadioField(
                    label = field.label,
                    options = options,
                    selectedValue = currentValue,
                    onValueChange = onValueChange,
                    isRequired = field.required,
                    enabled = field.isEditable
                )
                if (errorText != null) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            "file" -> {
                VsRegistrationFileField(
                    label = field.label,
                    value = currentValue.substringAfterLast("/"),
                    onClick = { filePickerLauncher.launch("*/*") },
                    placeholder = field.placeholder.ifEmpty { "Click to upload file" },
                    isRequired = field.required,
                    helperText = field.helper.takeIf { it.isNotEmpty() },
                    enabled = field.isEditable
                )
                if (errorText != null) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (isOtherSelected) {
            Spacer(modifier = Modifier.height(16.dp))
            VsRegistrationTextField(
                label = "Please specify",
                value = answers[field.name ] ?: "",
                onValueChange = { onAnswerUpdateByName(field.name, it) },
                placeholder = "Specify other",
                isRequired = field.required,
                enabled = field.isEditable
            )
        }
    }

    if (showDialog) {
        SelectionDialog(
            title = field.label,
            options = options,
            isMultiSelect = field.type == "multi-select",
            maxSelections = field.maxSelections,
            initialValue = currentValue,
            onDismiss = { showDialog = false },
            onSelect = { 
                onValueChange(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<EnrollmentRegistrationOption>,
    isMultiSelect: Boolean,
    maxSelections: Int,
    initialValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredOptions = options.filter { it.label.contains(searchQuery, ignoreCase = true) }
    
    val selectedValues = remember {
        mutableStateListOf<String>().apply {
            if (initialValue.isNotEmpty()) {
                addAll(initialValue.split("|"))
            }
        }
    }

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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isMultiSelect) {
                                        if (selectedValues.contains(option.value)) {
                                            selectedValues.remove(option.value)
                                        } else if (selectedValues.size < maxSelections) {
                                            selectedValues.add(option.value)
                                        }
                                    } else {
                                        onSelect(option.value)
                                    }
                                }
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isMultiSelect) {
                                Checkbox(
                                    checked = selectedValues.contains(option.value),
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.appColors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = option.label,
                                style = MaterialTheme.appTypography.bodyMedium,
                                color = Color.Black
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
                
                if (isMultiSelect) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onSelect(selectedValues.joinToString("|")) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.appColors.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun parseOptions(field: EnrollmentRegistrationField, answers: Map<String, String>): List<EnrollmentRegistrationOption> {
    val options = field.options ?: return emptyList()
    
    if (options is List<*>) {
        return options.filterIsInstance<EnrollmentRegistrationOption>()
    }
    
    return try {
        val gson = Gson()
        val json = gson.toJson(options)
        if (field.dependsOn.isNotEmpty()) {
            val type = object : TypeToken<Map<String, List<EnrollmentRegistrationOption>>>() {}.type
            val map = gson.fromJson<Map<String, List<EnrollmentRegistrationOption>>>(json, type)
            map[answers[field.dependsOn]] ?: emptyList()
        } else {
            val type = object : TypeToken<List<EnrollmentRegistrationOption>>() {}.type
            gson.fromJson(json, type)
        }
    } catch (_: Exception) {
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
