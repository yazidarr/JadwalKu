package com.example.ui.views

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.components.ScheduleCard
import com.example.ui.components.WeeklyTimetableGrid
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OnNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun WeekView(
    selectedDate: LocalDate,
    weekSchedules: Map<LocalDate, List<ScheduleItem>>,
    onDateSelect: (LocalDate) -> Unit,
    onCardClick: (ScheduleItem) -> Unit,
    onToggleComplete: (ScheduleItem) -> Unit,
    onShiftTime: (ScheduleItem, Int) -> Unit,
    onDelete: (ScheduleItem) -> Unit,
    onAddNew: () -> Unit,
    onAddAtSlot: ((LocalDate, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val localeId = Locale.forLanguageTag("id-ID")
    val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = monday.plusDays(6)
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

    val today = LocalDate.now()
    val daySchedules = weekSchedules[selectedDate].orEmpty()

    // Default to Weekly Grid Timetable layout!
    var showGridMode by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("week_view")
    ) {
        // Mode Sub-Header: Date Range & Toggle [Grid Mingguan] vs [Daftar Hari]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val rangeFmt = DateTimeFormatter.ofPattern("d MMM", localeId)
            Text(
                text = "${monday.format(rangeFmt)} - ${sunday.format(rangeFmt)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )

            // Segmented switch between Grid and List
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    Surface(
                        onClick = { showGridMode = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (showGridMode) ElectricBlue.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier.testTag("toggle_weekly_grid")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = if (showGridMode) ElectricBlue else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Grid",
                                fontSize = 11.sp,
                                fontWeight = if (showGridMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (showGridMode) TextWhite else TextMuted
                            )
                        }
                    }

                    Surface(
                        onClick = { showGridMode = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!showGridMode) ElectricBlue.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier.testTag("toggle_weekly_list")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewAgenda,
                                contentDescription = null,
                                tint = if (!showGridMode) ElectricBlue else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Daftar",
                                fontSize = 11.sp,
                                fontWeight = if (!showGridMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!showGridMode) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Body Content: Grid View or List View
        if (showGridMode) {
            // Weekly Timetable Grid Layout
            WeeklyTimetableGrid(
                selectedDate = selectedDate,
                weekSchedules = weekSchedules,
                onEventClick = onCardClick,
                onEmptySlotClick = { date, hour ->
                    if (onAddAtSlot != null) {
                        onAddAtSlot(date, hour)
                    } else {
                        onAddNew()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Day Strip + List View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Week Days Bar
                Surface(
                    color = DarkSurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekDays.forEach { date ->
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val itemCount = weekSchedules[date]?.size ?: 0

                            val dayName = when (date.dayOfWeek) {
                                DayOfWeek.MONDAY -> "Sen"
                                DayOfWeek.TUESDAY -> "Sel"
                                DayOfWeek.WEDNESDAY -> "Rab"
                                DayOfWeek.THURSDAY -> "Kam"
                                DayOfWeek.FRIDAY -> "Jum"
                                DayOfWeek.SATURDAY -> "Sab"
                                DayOfWeek.SUNDAY -> "Min"
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> ElectricBlue.copy(alpha = 0.25f)
                                            isToday -> NeonGreen.copy(alpha = 0.12f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isSelected -> ElectricBlue
                                            isToday -> NeonGreen.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onDateSelect(date) }
                                    .padding(vertical = 8.dp, horizontal = 2.dp)
                                    .testTag("week_day_chip_${date.dayOfMonth}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        isSelected -> ElectricBlue
                                        isToday -> NeonGreen
                                        else -> TextMuted
                                    }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${date.dayOfMonth}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || isToday) TextWhite else TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                if (itemCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isSelected) ElectricBlue else NeonGreen,
                                                CircleShape
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(6.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule List for Selected Day
                if (daySchedules.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(daySchedules, key = { it.id }) { item ->
                            ScheduleCard(
                                item = item,
                                onClick = { onCardClick(item) },
                                onToggleComplete = { onToggleComplete(item) },
                                onShiftTime = { mins -> onShiftTime(item, mins) },
                                onDelete = { onDelete(item) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                text = "Tidak ada agenda untuk hari ini",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = onAddNew,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGreen,
                                    contentColor = OnNeonGreen
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Tambah Agenda", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
