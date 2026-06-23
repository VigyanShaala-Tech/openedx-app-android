package org.openedx.auth.presentation.logistration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@Composable
fun LogistrationFilters(
    modifier: Modifier = Modifier,
    viewModel: LogistrationFiltersViewModel = koinViewModel(),
    onFiltersChanged: (selected: Map<String, String>) -> Unit = {}
) {
    val state by viewModel.state.collectAsState(FiltersState())
    val entries = state.options.options.entries.toList()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
