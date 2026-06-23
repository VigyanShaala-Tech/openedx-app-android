package org.openedx.course.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.openedx.core.domain.model.Block
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R
import org.openedx.course.presentation.contenttab.CourseContentAssignmentEmptyState

@Composable
fun AssignmentsHomePagerCardContent(
    uiState: CourseHomeUIState.CourseData,
    onAssignmentClick: (Block) -> Unit,
    onViewAllAssignmentsClick: () -> Unit,
    getBlockParent: (blockId: String) -> Block?,
) {
    if (uiState.courseAssignments.isEmpty()) {
        CourseContentAssignmentEmptyState(
            onReturnToCourseClick = {},
            showReturnButton = false
        )
        return
    }

    val completedAssignments = uiState.courseAssignments.count { it.isCompleted() }
    val totalAssignments = uiState.courseAssignments.size
    val firstIncompleteAssignment = uiState.courseAssignments.find { !it.isCompleted() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Assignments",
            style = MaterialTheme.appTypography.titleLarge,
            color = MaterialTheme.appColors.textDark,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header with progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                tint = MaterialTheme.appColors.textDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$completedAssignments/$totalAssignments",
                style = MaterialTheme.appTypography.displaySmall,
                color = MaterialTheme.appColors.textDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Assignments",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                )
                Text(
                    text = "completed",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Divider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.appColors.cardViewBorder.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(20.dp))

        // First Incomplete Assignment section
        if (firstIncompleteAssignment != null) {
            AssignmentCard(
                assignment = firstIncompleteAssignment,
                sectionName = getBlockParent(firstIncompleteAssignment.id)?.displayName ?: "",
                onAssignmentClick = onAssignmentClick,
                background = MaterialTheme.appColors.background,
            )
        }
else {
            CaughtUpMessage(
                message = stringResource(R.string.course_assignments_caught_up)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // View All Assignments button
        ViewAllButton(
            text = stringResource(R.string.course_view_all_assignments),
            onClick = onViewAllAssignmentsClick
        )
    }
}

@Composable
private fun AssignmentCard(
    assignment: Block,
    sectionName: String,
    onAssignmentClick: (Block) -> Unit,
    background: Color = MaterialTheme.appColors.surface
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAssignmentClick(assignment) },
        backgroundColor = background,
        border = BorderStroke(1.dp, MaterialTheme.appColors.cardViewBorder),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Assignment and section name
                Text(
                    text = assignment.displayName ?: "",
                    style = MaterialTheme.appTypography.titleSmall,
                    color = MaterialTheme.appColors.textDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sectionName,
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textSecondary,
                )
            }

            // Chevron arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.appColors.textDark
            )
        }
    }
}
