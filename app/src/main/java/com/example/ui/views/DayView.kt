package com.example.ui.views

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.components.ScheduleCard
import com.example.ui.components.TimeIndicatorLine
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DayView(
    selectedDate: LocalDate,
    schedules: List<ScheduleItem>,
    onCardClick: (ScheduleItem) -> Unit,
    onToggleComplete: (ScheduleItem) -> Unit,
    onShiftTime: (ScheduleItem, Int) -> Unit,
    onDelete: (ScheduleItem) -> Unit,
    onAddAtHour: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = selectedDate == LocalDate.now()
    val currentHour = LocalTime.now().hour
    val listState = rememberLazyListState()

    // Auto-scroll to near current hour if today
    LaunchedEffect(selectedDate) {
        if (isToday && currentHour > 2) {
            listState.scrollToItem((currentHour - 1).coerceAtLeast(0))
        }
    }

    // Group schedules by start hour
    val schedulesByHour = schedules.groupBy { it.startHour }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("day_view_timeline"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Render 24 hours (from 06:00 to 23:00, then early morning 00:00 - 05:00, or standard 24 hours 0..23)
        // Let's render hours 0..23
        items(24) { hour ->
            val hourSchedules = schedulesByHour[hour].orEmpty()
            val isCurrentHour = isToday && currentHour == hour

            Column(modifier = Modifier.fillMaxWidth()) {
                // Real-time indicator line placed if today and in this hour
                if (isToday && currentHour == hour) {
                    TimeIndicatorLine(modifier = Modifier.padding(bottom = 6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Hour label column
                    Column(
                        modifier = Modifier.width(52.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = String.format("%02d:00", hour),
                            fontSize = 12.sp,
                            fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrentHour) ElectricBlue else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Schedule Blocks or Empty Hour Slot
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (hourSchedules.isNotEmpty()) {
                            hourSchedules.forEach { item ->
                                ScheduleCard(
                                    item = item,
                                    onClick = { onCardClick(item) },
                                    onToggleComplete = { onToggleComplete(item) },
                                    onShiftTime = { mins -> onShiftTime(item, mins) },
                                    onDelete = { onDelete(item) }
                                )
                            }
                        } else {
                            // Empty Hour Clickable Slot
                            EmptyHourSlot(
                                hour = hour,
                                isCurrentHour = isCurrentHour,
                                onClick = { onAddAtHour(hour) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHourSlot(
    hour: Int,
    isCurrentHour: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isCurrentHour) ElectricBlue.copy(alpha = 0.05f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentHour) ElectricBlue.copy(alpha = 0.2f) else DarkBorder.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .testTag("empty_slot_$hour")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isCurrentHour) "Slot aktif sekarang" else "Ketuk untuk tambah agenda",
                fontSize = 11.sp,
                color = if (isCurrentHour) ElectricBlue.copy(alpha = 0.7f) else TextMuted.copy(alpha = 0.6f)
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = if (isCurrentHour) ElectricBlue else TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
