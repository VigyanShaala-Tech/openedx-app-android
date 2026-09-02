package org.openedx.course.presentation.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.R as coreR

@Composable
fun RegistrationStepper(
    modifier: Modifier = Modifier,
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            RegistrationStepCircle(
                step = i,
                isCompleted = i < currentStep,
                isSelected = i == currentStep
            )
            if (i < totalSteps) {
                Divider(
                    modifier = Modifier.width(60.dp),
                    color = if (i < currentStep) Color(0xFF4CAF50) else MaterialTheme.appColors.divider,
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun RegistrationStepCircle(
    step: Int,
    isCompleted: Boolean,
    isSelected: Boolean
) {
    val backgroundColor = when {
        isCompleted -> Color(0xFF4CAF50)
        isSelected -> MaterialTheme.appColors.background
        else -> MaterialTheme.appColors.background
    }
    val borderColor = when {
        isCompleted -> Color(0xFF4CAF50)
        isSelected -> Color(0xFF4CAF50)
        else -> MaterialTheme.appColors.divider
    }
    val textColor = when {
        isCompleted -> Color.White
        isSelected -> Color(0xFF4CAF50)
        else -> MaterialTheme.appColors.textSecondary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(backgroundColor, CircleShape)
            .border(1.dp, borderColor, CircleShape),
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
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun VsRegistrationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isRequired: Boolean = false,
    isTextArea: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.appColors.textDark
        )
        if (!helperText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textFieldHint
                )
            },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                focusedBorderColor = MaterialTheme.appColors.primary,
                backgroundColor = if (enabled) MaterialTheme.appColors.textFieldBackground else MaterialTheme.appColors.surface,
                textColor = MaterialTheme.appColors.textFieldText,
                cursorColor = MaterialTheme.appColors.textFieldText,
                disabledTextColor = MaterialTheme.appColors.textSecondary,
                disabledBorderColor = MaterialTheme.appColors.divider
            ),
            isError = errorText != null,
            minLines = if (isTextArea) 3 else 1,
            maxLines = if (isTextArea) 5 else 1,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
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

@Composable
fun VsRegistrationSelectField(
    label: String,
    value: String,
    onClick: () -> Unit,
    placeholder: String,
    isRequired: Boolean = false,
    helperText: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.appColors.textDark
        )
        if (!helperText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(if (enabled) MaterialTheme.appColors.textFieldBackground else MaterialTheme.appColors.surface, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.appColors.textFieldBorder, RoundedCornerShape(8.dp))
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.appTypography.bodyMedium,
                color = if (value.isEmpty()) MaterialTheme.appColors.textFieldHint else MaterialTheme.appColors.textDark,
                modifier = Modifier.padding(end = 24.dp)
            )
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd).size(32.dp),
                    tint = MaterialTheme.appColors.textDark
                )
            }
        }
    }
}

@Composable
fun VsRegistrationRadioField(
    label: String,
    options: List<org.openedx.core.domain.model.EnrollmentRegistrationOption>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.appColors.textDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (enabled) Color.Transparent else MaterialTheme.appColors.surface,
                    shape = RoundedCornerShape(8.dp)
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = enabled) { onValueChange(option.value) }
                        .padding(end = 16.dp)
                ) {
                    RadioButton(
                        selected = selectedValue == option.value,
                        onClick = if (enabled) { { onValueChange(option.value) } } else null,
                        enabled = enabled,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.appColors.primary,
                            unselectedColor = MaterialTheme.appColors.textSecondary,
                            disabledColor = if (selectedValue == option.value) MaterialTheme.appColors.primary else MaterialTheme.appColors.divider
                        )
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = MaterialTheme.appColors.textDark,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VsRegistrationFileField(
    label: String,
    value: String,
    onClick: () -> Unit,
    placeholder: String,
    isRequired: Boolean = false,
    helperText: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isRequired) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.appColors.textDark
        )
        if (!helperText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(if (enabled) MaterialTheme.appColors.textFieldBackground else MaterialTheme.appColors.surface, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.appColors.textFieldBorder, RoundedCornerShape(8.dp))
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.appTypography.bodyMedium,
                color = if (value.isEmpty()) MaterialTheme.appColors.textFieldHint else MaterialTheme.appColors.textDark,
                modifier = Modifier.padding(end = 24.dp)
            )
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp),
                    tint = MaterialTheme.appColors.textDark
                )
            }
        }
    }
}
