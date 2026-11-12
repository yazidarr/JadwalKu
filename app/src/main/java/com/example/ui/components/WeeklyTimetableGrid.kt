package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.theme.DarkBackground
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeeklyTimetableGrid(
    selectedDate: LocalDate,
    weekSchedules: Map<LocalDate, List<ScheduleItem>>,
    onEventClick: (ScheduleItem) -> Unit,
    onEmptySlotClick: (LocalDate, Int) -> Unit,
    modifier: Modifier = Modifier,
    startHour: Int = 7,
    endHour: Int = 22,
    columnWidthDp: Int = 96,
    timeColumnWidthDp: Int = 52,
    hourRowHeightDp: Int = 64
) {
    val monday = remember(selectedDate) {
        selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val weekDays = remember(monday) {
        (0..6).map { monday.plusDays(it.toLong()) }
    }
    val today = LocalDate.now()
    val currentHour = LocalTime.now().hour

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Auto-scroll vertically to current hour on load
    LaunchedEffect(Unit) {
        if (currentHour in startHour..endHour) {
            val scrollTarget = (currentHour - startHour - 1).coerceAtLeast(0) * hourRowHeightDp
            verticalScrollState.animateScrollTo(scrollTarget * 2) // approximate density
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("weekly_grid_layout")
    ) {
        // 1. Fixed Header Row with Weekday Columns (Synchronized horizontally)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .border(width = 1.dp, color = DarkBorder)
        ) {
            // Corner spacer for time column
            Box(
                modifier = Modifier
                    .width(timeColumnWidthDp.dp)
                    .height(52.dp)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waktu",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }

            // Scrollable day column headers
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                weekDays.forEach { date ->
                    val isToday = date == today
                    val isSelected = date == selectedDate
                    val dayInitial = when (date.dayOfWeek) {
                        DayOfWeek.MONDAY -> "Sen"
                        DayOfWeek.TUESDAY -> "Sel"
                        DayOfWeek.WEDNESDAY -> "Rab"
                        DayOfWeek.THURSDAY -> "Kam"
                        DayOfWeek.FRIDAY -> "Jum"
                        DayOfWeek.SATURDAY -> "Sab"
                        DayOfWeek.SUNDAY -> "Min"
                    }
                    val eventCount = weekSchedules[date]?.size ?: 0

                    Box(
                        modifier = Modifier
                            .width(columnWidthDp.dp)
                            .height(52.dp)
                            .background(
                                when {
                                    isToday -> NeonGreen.copy(alpha = 0.12f)
                                    isSelected -> ElectricBlue.copy(alpha = 0.1f)
                                    else -> DarkSurface
                                }
                            )
                            .border(width = 0.5.dp, color = DarkBorder)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayInitial,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    isToday -> NeonGreen
                                    isSelected -> ElectricBlue
                                    else -> TextSecondary
                                }
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${date.dayOfMonth}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday || isSelected) TextWhite else TextPrimary
                                )

                                if (eventCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(
                                                if (isToday) NeonGreen else ElectricBlue,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Scrollable Grid Body (Vertical & Horizontal synchronized)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
        ) {
            // Time Labels Column (Vertical only)
            Column(
                modifier = Modifier
                    .width(timeColumnWidthDp.dp)
                    .background(DarkSurfaceElevated)
            ) {
                for (hour in startHour..endHour) {
                    val isCurrentHour = (hour == currentHour)
                    Box(
                        modifier = Modifier
                            .width(timeColumnWidthDp.dp)
                            .height(hourRowHeightDp.dp)
                            .border(width = 0.5.dp, color = DarkBorder)
                            .padding(end = 6.dp, top = 4.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = String.format("%02d:00", hour),
                            fontSize = 11.sp,
                            fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrentHour) ElectricBlue else TextMuted
                        )
                    }
                }
            }

            // Grid Cells for all 7 Days (Horizontally scrollable with header)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                weekDays.forEach { date ->
                    val isToday = date == today
                    val dayEvents = weekSchedules[date].orEmpty()
                    val eventsByHour = dayEvents.groupBy { it.startHour }

                    Column(
                        modifier = Modifier
                            .width(columnWidthDp.dp)
                            .background(if (isToday) NeonGreen.copy(alpha = 0.03f) else DarkBackground)
                    ) {
                        for (hour in startHour..endHour) {
                            val hourEvents = eventsByHour[hour].orEmpty()
                            val isCurrentHourSlot = isToday && (hour == currentHour)

                            Box(
                                modifier = Modifier
                                    .width(columnWidthDp.dp)
                                    .height(hourRowHeightDp.dp)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isCurrentHourSlot) ElectricBlue.copy(alpha = 0.3f) else DarkBorder
                                    )
                                    .clickable {
                                        if (hourEvents.isEmpty()) {
                                            onEmptySlotClick(date, hour)
                                        }
                                    }
                                    .padding(2.dp)
                                    .testTag("grid_cell_${date}_$hour")
                            ) {
                                if (hourEvents.isNotEmpty()) {
                                    // Render Event Blocks in this hour
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        hourEvents.forEach { item ->
                                            WeeklyGridEventBlock(
                                                item = item,
                                                onClick = { onEventClick(item) }
                                            )
                                        }
                                    }
                                } else {
                                    // Empty slot subtle hint on hover/press
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCurrentHourSlot) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(ElectricBlue)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyGridEventBlock(
    item: ScheduleItem,
    onClick: () -> Unit
) {
    val category = item.categoryEnum
    val isCompleted = item.isCompleted

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isCompleted) DarkSurfaceElevated else category.color.copy(alpha = 0.22f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) NeonGreen.copy(alpha = 0.5f) else category.color
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_event_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.startTime,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) NeonGreen else category.color
                )

                if (item.isReminderEnabled) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Pengingat",
                        tint = ElectricBlue,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }

            Text(
                text = item.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCompleted) TextMuted else TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
