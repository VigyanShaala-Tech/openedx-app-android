package org.openedx.course.presentation.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
                    color = if (i < currentStep) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
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
        isSelected -> Color.White
        else -> Color.White
    }
    val borderColor = when {
        isCompleted -> Color(0xFF4CAF50)
        isSelected -> Color(0xFF4CAF50)
        else -> Color(0xFFE0E0E0)
    }
    val textColor = when {
        isCompleted -> Color.White
        isSelected -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
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
            color = Color(0xFF424242)
        )
        if (!helperText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.appTypography.labelSmall,
                color = Color(0xFF757575)
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
                    color = Color(0xFF9E9E9E)
                )
            },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFFE0E0E0),
                backgroundColor = if (enabled) Color.White else Color(0xFFF5F5F5),
                textColor = Color.Black,
                cursorColor = Color.Black,
                disabledTextColor = Color(0xFF757575),
                disabledBorderColor = Color(0xFFE0E0E0)
            ),
            isError = errorText != null,
            minLines = if (isTextArea) 3 else 1,
            maxLines = if (isTextArea) 5 else 1
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
            color = Color(0xFF424242)
        )
        if (!helperText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.appTypography.bodySmall,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(if (enabled) Color.White else Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.appTypography.bodyMedium,
                color = if (value.isEmpty()) Color(0xFF9E9E9E) else if (enabled) Color.Black else Color(0xFF757575),
                modifier = Modifier.padding(end = 24.dp)
            )
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd).size(32.dp),
                    tint = Color.Black
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
            color = Color(0xFF424242)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (enabled) Color.Transparent else Color(0xFFF5F5F5),
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
                            selectedColor = Color(0xFF2196F3),
                            unselectedColor = Color(0xFF757575),
                            disabledColor = if (selectedValue == option.value) Color(0xFF2196F3) else Color(0xFFBDBDBD)
                        )
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = if (enabled) Color.Black else Color(0xFF757575),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
