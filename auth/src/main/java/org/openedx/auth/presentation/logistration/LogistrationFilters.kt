package org.openedx.auth.presentation.logistration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography

@Composable
fun LogistrationFilters(
    modifier: Modifier = Modifier,
    viewModel: LogistrationFiltersViewModel = koinViewModel(),
    onFiltersChanged: (category: String, level: String, subject: String) -> Unit = { _, _, _ -> }
) {
    val state by viewModel.state.collectAsState(FiltersState())
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill(
            label = state.selectedCategory,
            options = state.options.categories,
            onSelect = {
                viewModel.selectCategory(it)
                onFiltersChanged(viewModel.state.value.selectedCategory, viewModel.state.value.selectedLevel, viewModel.state.value.selectedSubject)
            }
        )
        FilterPill(
            label = state.selectedLevel,
            options = state.options.levels,
            onSelect = {
                viewModel.selectLevel(it)
                onFiltersChanged(viewModel.state.value.selectedCategory, viewModel.state.value.selectedLevel, viewModel.state.value.selectedSubject)
            }
        )
        FilterPill(
            label = state.selectedSubject,
            options = state.options.subjects,
            onSelect = {
                viewModel.selectSubject(it)
                onFiltersChanged(viewModel.state.value.selectedCategory, viewModel.state.value.selectedLevel, viewModel.state.value.selectedSubject)
            }
        )
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun FilterPill(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.appColors.textFieldBackground, MaterialTheme.appShapes.textFieldShape)
            .clickable { expanded.value = true }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.appTypography.bodySmall,
            color = MaterialTheme.appColors.textPrimary
        )
        Spacer(Modifier.size(6.dp))
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.appColors.textPrimary
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            options.forEach { opt ->
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    onSelect(opt)
                }) {
                    Text(text = opt, style = MaterialTheme.appTypography.labelLarge)
                }
            }
        }
    }
}
