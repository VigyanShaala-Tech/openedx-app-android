package org.openedx.auth.presentation.logistration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.openedx.core.ui.horizontalScrollbar
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogistrationFilters(
    modifier: Modifier = Modifier,
    viewModel: LogistrationFiltersViewModel = koinViewModel(),
    onFiltersChanged: (selected: Map<String, String>) -> Unit = {}
) {
    val state by viewModel.state.collectAsState(FiltersState())
    val entries = state.options.options.entries.toList()
    val scrollState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScrollbar(scrollState, MaterialTheme.appColors.primary),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 8.dp)
        ) {
            items(entries) { (key, options) ->
                val label = state.selected[key] ?: options.firstOrNull().orEmpty()
                FilterPill(
                    label = label,
                    options = options,
                    onSelect = {
                        viewModel.select(key, it)
                        onFiltersChanged(viewModel.state.value.selected)
                    }
                )
            }
        }

        // Active Filters section
        val activeFilters = state.selected.filter { (key, value) ->
            val options = state.options.options[key]
            val defaultOption = options?.firstOrNull().orEmpty()
            value.isNotEmpty() && value != defaultOption
        }

        if (activeFilters.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeFilters.forEach { (key, value) ->
                    ActiveFilterChip(
                        text = value,
                        onRemove = {
                            val defaultOption = state.options.options[key]?.firstOrNull().orEmpty()
                            viewModel.select(key, defaultOption)
                            onFiltersChanged(viewModel.state.value.selected)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.appColors.primary.copy(alpha = 0.5f)),
        color = MaterialTheme.appColors.primary.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.primary
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Row(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(16.dp)) // Refined rounded corners
                .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.appTypography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF546E7A)
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = Color(0xFF78909C),
                modifier = Modifier.size(16.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .clip(RoundedCornerShape(8.dp)) // Rounded corners for dropdown menu
        ) {
            options.forEach { opt ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onSelect(opt)
                }) {
                    Text(
                        text = opt, 
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = Color(0xFF263238)
                    )
                }
            }
        }
    }
}
