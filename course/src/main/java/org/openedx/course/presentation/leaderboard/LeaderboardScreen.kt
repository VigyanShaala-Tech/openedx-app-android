package org.openedx.course.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.data.model.University
import org.openedx.course.data.model.RankingOption
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.UIMessage

@Composable
fun LeaderboardScreen(
    windowSize: WindowSize,
    viewModel: LeaderboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

    LeaderboardUI(
        uiState = uiState,
        uiMessage = uiMessage,
        onUniversitySelected = viewModel::onUniversitySelected,
        onRankingOptionSelected = viewModel::onRankingOptionSelected,
        onLoadMore = viewModel::loadMore
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LeaderboardUI(
    uiState: LeaderboardUIState,
    uiMessage: UIMessage?,
    onUniversitySelected: (University?) -> Unit,
    onRankingOptionSelected: (RankingOption) -> Unit,
    onLoadMore: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background
    ) { padding ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Leaderboard",
                style = MaterialTheme.appTypography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textDark,
                    fontSize = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current User Rank Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.appColors.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFFF1F8E9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFF8BC34A),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "YOUR CURRENT POSITION",
                            style = MaterialTheme.appTypography.bodySmall,
                            color = MaterialTheme.appColors.textSecondary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Rank #${uiState.userRanking?.rank ?: 0}",
                                style = MaterialTheme.appTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.appColors.textDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "with ",
                                style = MaterialTheme.appTypography.bodyMedium,
                                color = MaterialTheme.appColors.textSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "${uiState.userRanking?.points ?: 0} points",
                                style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF8BC34A),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // University Filter
            LeaderboardFilter(
                label = uiState.selectedUniversity?.name ?: "All Colleges",
                options = listOf(University(0, "All Colleges")) + uiState.universities,
                onSelected = { if (it.id == 0) onUniversitySelected(null) else onUniversitySelected(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ranking Filter
            LeaderboardFilter(
                label = uiState.selectedRankingOption.name,
                options = uiState.rankingOptions,
                onSelected = onRankingOptionSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Leaderboard Table
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.appColors.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(text = "Rank", modifier = Modifier.width(60.dp), style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
                        Text(text = "User", modifier = Modifier.weight(1f), style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
                        Text(text = "College", modifier = Modifier.weight(1f), style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
                        Text(text = "Points", modifier = Modifier.width(60.dp), textAlign = TextAlign.End, style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(uiState.leaderboardEntries) { index, entry ->
                            LeaderboardRow(entry)
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                            
                            if (index == uiState.leaderboardEntries.size - 1 && uiState.hasMore) {
                                LaunchedEffect(Unit) {
                                    onLoadMore()
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LeaderboardRow(entry: org.openedx.course.data.model.LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#${entry.rank}",
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.appColors.textDark
        )
        Text(
            text = entry.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.appColors.textDark
        )
        Text(
            text = entry.university ?: "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.appTypography.bodySmall,
            color = MaterialTheme.appColors.textSecondary
        )
        Text(
            text = "${entry.points}",
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF8BC34A)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun <T> LeaderboardFilter(
    label: String,
    options: List<T>,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF8BC34A),
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                ) {
                    val text = when (option) {
                        is University -> option.name
                        is RankingOption -> option.name
                        else -> option.toString()
                    }
                    Text(text = text)
                }
            }
        }
    }
}
