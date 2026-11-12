package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCard(
    item: ScheduleItem,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onShiftTime: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swiped Right -> Toggle Complete
                    onToggleComplete()
                    false // Don't dismiss from view, snap back with updated status
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swiped Left -> Delete
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(vertical = 4.dp),
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> NeonGreen.copy(alpha = 0.35f)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF5252).copy(alpha = 0.35f)
                    else -> Color.Transparent
                },
                label = "swipe_bg"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Tandai Selesai",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (item.isCompleted) "Batal Selesai" else "Tandai Selesai",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hapus Agenda",
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Agenda",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        CardContent(
            item = item,
            onClick = onClick,
            onToggleComplete = onToggleComplete,
            onShiftTime = onShiftTime
        )
    }
}

@Composable
private fun CardContent(
    item: ScheduleItem,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onShiftTime: (Int) -> Unit
) {
    val category = item.categoryEnum
    val isCompleted = item.isCompleted

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted) DarkSurfaceElevated.copy(alpha = 0.8f) else DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) NeonGreen.copy(alpha = 0.4f) else DarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Vertical Stripe indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCompleted) NeonGreen else category.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main details
            Column(modifier = Modifier.weight(1f)) {
                // Top row: Time range and Reminder Bell badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${item.startTime} - ${item.endTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricBlue
                        )
                    }

                    // Top-right icons: Reminder Bell (if enabled) + Category Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (item.isReminderEnabled) {
                            Surface(
                                color = ElectricBlue.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Pengingat Aktif",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Category badge
                        Surface(
                            color = category.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = category.displayName,
                                color = category.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) TextMuted else TextWhite,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional Description / Location
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Direct Action Bar: Quick Shift & Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quick Shift buttons (+15m, +30m, +1h)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickShiftChip(
                            label = "-15m",
                            onClick = { onShiftTime(-15) },
                            testTag = "shift_minus_15_${item.id}"
                        )
                        QuickShiftChip(
                            label = "+15m",
                            onClick = { onShiftTime(15) },
                            testTag = "shift_plus_15_${item.id}"
                        )
                        QuickShiftChip(
                            label = "+1j",
                            onClick = { onShiftTime(60) },
                            testTag = "shift_plus_60_${item.id}"
                        )
                    }

                    // Complete Toggle Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleComplete() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("toggle_complete_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isCompleted) "Selesai" else "Belum Selesai",
                            tint = if (isCompleted) NeonGreen else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCompleted) "Selesai" else "Tandai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) NeonGreen else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickShiftChip(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
