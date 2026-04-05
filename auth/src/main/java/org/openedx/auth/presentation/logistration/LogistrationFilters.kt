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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
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
    
    val sortOptions = listOf("Popular Courses", "Newly Added", "Top Rated")
    var selectedSort by remember { mutableStateOf(sortOptions[0]) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Dropdowns in Grid-like layout
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Sort Dropdown - matches "English" dropdown style in image 3
            SortFilterPill(
                modifier = Modifier.fillMaxWidth(),
                selectedOption = selectedSort,
                options = sortOptions,
                onSelect = {
                    selectedSort = it
                    val newFilters = viewModel.state.value.selected.toMutableMap()
                    newFilters["sort"] = it
                    onFiltersChanged(newFilters)
                }
            )
            
            // First Filter - matches image grid
            if (entries.isNotEmpty()) {
                val (key, options) = entries[0]
                val label = state.selected[key] ?: options.firstOrNull().orEmpty()
                FilterPill(
                    modifier = Modifier.fillMaxWidth(),
                    label = label,
                    options = options,
                    onSelect = {
                        viewModel.select(key, it)
                        val newFilters = viewModel.state.value.selected.toMutableMap()
                        newFilters["sort"] = selectedSort
                        onFiltersChanged(newFilters)
                    }
                )
            }
            
            // Next two filters side by side
            if (entries.size >= 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1 until minOf(3, entries.size)) {
                        val (key, options) = entries[i]
                        val label = state.selected[key] ?: options.firstOrNull().orEmpty()
                        FilterPill(
                            modifier = Modifier.weight(1f),
                            label = label,
                            options = options,
                            onSelect = {
                                viewModel.select(key, it)
                                val newFilters = viewModel.state.value.selected.toMutableMap()
                                newFilters["sort"] = selectedSort
                                onFiltersChanged(newFilters)
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Divider(color = MaterialTheme.appColors.textFieldBorder)
        Spacer(Modifier.height(12.dp))

        // Active Filters section
        val activeFilters = state.selected.filter { (key, value) ->
            val options = state.options.options[key]
            val defaultOption = options?.firstOrNull().orEmpty()
            value != defaultOption
        }

        if (activeFilters.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Active filters: ",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.width(8.dp))
                
                FlowRow(
                    modifier = Modifier.weight(1f),
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
                    
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.appTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clickable {
                                viewModel.reset()
                                selectedSort = sortOptions[0]
                                onFiltersChanged(viewModel.state.value.selected)
                            }
                            .padding(vertical = 4.dp)
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
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.appColors.primary.copy(alpha = 0.5f)),
        color = MaterialTheme.appColors.primary.copy(alpha = 0.05f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.primary
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun SortFilterPill(
    modifier: Modifier = Modifier,
    selectedOption: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.appColors.textFieldBorder, RoundedCornerShape(8.dp))
                .background(MaterialTheme.appColors.background)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedOption,
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.appColors.textDark,
                modifier = Modifier.size(20.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(if (modifier == Modifier.fillMaxWidth()) 1f else 0.5f)
                .background(Color.White)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelect(opt)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = opt, 
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textDark
                        )
                        if (opt == selectedOption) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.appColors.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.appColors.textFieldBorder, RoundedCornerShape(8.dp))
                .background(MaterialTheme.appColors.background)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.appColors.textDark,
                modifier = Modifier.size(20.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onSelect(opt)
                }) {
                    Text(
                        text = opt, 
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = MaterialTheme.appColors.textDark
                    )
                }
            }
        }
    }
}
