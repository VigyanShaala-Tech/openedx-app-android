package org.openedx.core.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimens(
    val screenHorizontalPadding: Dp = 16.dp,
    val screenVerticalPadding: Dp = 16.dp,
    val cardCornerRadius: Dp = 16.dp,
    val buttonCornerRadius: Dp = 8.dp,
    val chipCornerRadius: Dp = 12.dp,
    val textFieldCornerRadius: Dp = 8.dp,
    val defaultPadding: Dp = 8.dp,
    val doublePadding: Dp = 16.dp,
    val halfPadding: Dp = 4.dp,
    val toolbarHeight: Dp = 56.dp,
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp,
)

val LocalDimens = staticCompositionLocalOf { AppDimens() }

val MaterialTheme.appDimens: AppDimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimens.current
