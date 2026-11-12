package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.components.ScheduleCard
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthView(
    selectedDate: LocalDate,
    allSchedules: List<ScheduleItem>,
    onDateSelect: (LocalDate) -> Unit,
    onCardClick: (ScheduleItem) -> Unit,
    onToggleComplete: (ScheduleItem) -> Unit,
    onShiftTime: (ScheduleItem, Int) -> Unit,
    onDelete: (ScheduleItem) -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localeId = Locale.forLanguageTag("id-ID")
    var currentYearMonth by remember(selectedDate) {
        mutableStateOf(YearMonth.of(selectedDate.year, selectedDate.month))
    }

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Map schedules count per date string
    val schedulesByDate = remember(allSchedules) {
        allSchedules.groupBy { it.date }
    }

    val selectedDateStr = selectedDate.format(dateFormatter)
    val daySchedules = schedulesByDate[selectedDateStr].orEmpty()

    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("month_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Calendar Card
        item {
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentYearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Bulan Sebelumnya",
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Bulan Berikutnya",
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of week header (Senin - Minggu)
                    val daysOfWeekHeaders = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        daysOfWeekHeaders.forEach { name ->
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid
                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    val daysInMonth = currentYearMonth.lengthOfMonth()

                    // DayOfWeek: MONDAY = 1 ... SUNDAY = 7
                    val leadingEmptyDays = firstDayOfMonth.dayOfWeek.value - 1
                    val totalSlots = (leadingEmptyDays + daysInMonth + 6) / 7 * 7

                    val rows = totalSlots / 7

                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0..6) {
                                val slotIndex = row * 7 + col
                                val dayNumber = slotIndex - leadingEmptyDays + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = currentYearMonth.atDay(dayNumber)
                                    val cellDateStr = cellDate.format(dateFormatter)
                                    val isSelected = cellDate == selectedDate
                                    val isCurrentDay = cellDate == today
                                    val hasEvents = (schedulesByDate[cellDateStr]?.size ?: 0) > 0

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> ElectricBlue.copy(alpha = 0.25f)
                                                    isCurrentDay -> NeonGreen.copy(alpha = 0.12f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when {
                                                    isSelected -> ElectricBlue
                                                    isCurrentDay -> NeonGreen.copy(alpha = 0.5f)
                                                    else -> Color.Transparent
                                                },
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onDateSelect(cellDate) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> ElectricBlue
                                                    isCurrentDay -> NeonGreen
                                                    else -> TextWhite
                                                }
                                            )

                                            if (hasEvents) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .size(4.dp)
                                                        .background(
                                                            if (isSelected) ElectricBlue else NeonGreen,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Agenda List for Selected Day
        item {
            val fullFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Agenda ${selectedDate.format(fullFormatter)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = OnNeonGreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    ),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (daySchedules.isNotEmpty()) {
            items(daySchedules, key = { it.id }) { item ->
                ScheduleCard(
                    item = item,
                    onClick = { onCardClick(item) },
                    onToggleComplete = { onToggleComplete(item) },
                    onShiftTime = { mins -> onShiftTime(item, mins) },
                    onDelete = { onDelete(item) }
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada agenda di tanggal ini.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
