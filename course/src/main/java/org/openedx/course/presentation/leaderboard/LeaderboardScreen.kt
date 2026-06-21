package org.openedx.course.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
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
                    label = uiState.selectedUniversity?.let { it.name ?: it.universityName ?: it.universityNameSnake ?: it.collegeName ?: it.collegeNameSnake ?: it.title ?: it.label ?: it.text ?: it.value } ?: "All Colleges",
                    title = "Select College",
                    options = uiState.universities,
                    labelExtractor = { it.name ?: it.universityName ?: it.universityNameSnake ?: it.collegeName ?: it.collegeNameSnake ?: it.title ?: it.label ?: it.text ?: it.value ?: "" },
                    onSelected = onUniversitySelected
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ranking Filter
                LeaderboardFilter(
                    label = uiState.selectedRankingOption.let { it.label ?: it.name ?: it.displayName ?: "" },
                    title = "Select Filter",
                    options = uiState.rankingOptions,
                    labelExtractor = { it.label ?: it.name ?: it.displayName ?: "" },
                    onSelected = onRankingOptionSelected
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Leaderboard Table Header
                LeaderboardHeader()
            }

            if (uiState.isLoading && uiState.leaderboardEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                    }
                }
            } else if (uiState.leaderboardEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No rankings available",
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textSecondary
                        )
                    }
                }
            } else {
                itemsIndexed(uiState.leaderboardEntries) { index, entry ->
                    LeaderboardRow(entry)
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    if (index == uiState.leaderboardEntries.size - 1 && uiState.hasMore) {
                        LaunchedEffect(Unit) {
                            onLoadMore()
                        }
                    }
                }
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.appColors.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LeaderboardHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        elevation = 0.dp,
        backgroundColor = Color(0xFFF9FAFB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text = "Rank", modifier = Modifier.width(60.dp), style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
            Text(text = "User", modifier = Modifier.weight(1f), style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
            Text(text = "College", modifier = Modifier.weight(1f), style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
            Text(text = "Points", modifier = Modifier.width(60.dp), textAlign = TextAlign.End, style = MaterialTheme.appTypography.labelLarge, color = MaterialTheme.appColors.textSecondary)
        }
    }
    Divider(color = Color.LightGray.copy(alpha = 0.5f))
}

@Composable
private fun LeaderboardRow(entry: org.openedx.course.data.model.LeaderboardEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.appColors.background,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
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
}

@Composable
private fun <T> LeaderboardFilter(
    label: String?,
    title: String,
    options: List<T>,
    labelExtractor: (T) -> String?,
    onSelected: (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = label ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF8BC34A),
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
        // Overlay to capture clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.appTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Divider()
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(options) { option ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelected(option)
                                        showDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = labelExtractor(option) ?: "",
                                    style = MaterialTheme.appTypography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("CANCEL", color = MaterialTheme.appColors.primary)
                        }
                    }
                }
            }
        }
    }
}
