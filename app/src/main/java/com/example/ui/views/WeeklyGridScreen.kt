package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.ui.components.ScheduleDetailBottomSheet
import com.example.ui.components.WeeklyTimetableGrid
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OnNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ScheduleViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyGridScreen(
    viewModel: ScheduleViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val weekSchedules by viewModel.currentWeekSchedules.collectAsState()
    val editingSchedule by viewModel.editingSchedule.collectAsState()
    val isNewModalOpen by viewModel.isNewScheduleModalOpen.collectAsState()

    val localeId = Locale.forLanguageTag("id-ID")
    val monday = remember(selectedDate) {
        selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val sunday = remember(monday) { monday.plusDays(6) }

    val rangeFormatter = DateTimeFormatter.ofPattern("d MMM", localeId)
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy", localeId)
    val weekRangeText = "${monday.format(rangeFormatter)} - ${sunday.format(rangeFormatter)} ${sunday.format(yearFormatter)}"

    val isCurrentWeek = remember(monday) {
        val todayMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        monday == todayMonday
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("weekly_grid_screen"),
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Grid Mingguan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = weekRangeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ElectricBlue
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("weekly_grid_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TextWhite
                            )
                        }
                    } else {
                        // Quick jump to Today if not on current week
                        IconButton(
                            onClick = { viewModel.jumpToToday() },
                            modifier = Modifier.testTag("weekly_grid_today_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Minggu Ini",
                                tint = if (isCurrentWeek) NeonGreen else TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Navigate previous/next week
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.navigateDateBy(-7) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("prev_week_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Minggu Lalu",
                                tint = TextWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.navigateDateBy(7) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("next_week_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Minggu Depan",
                                tint = TextWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        floatingActionButton = {
            // Material3 Floating Action Button for quick event addition
            ExtendedFloatingActionButton(
                onClick = { viewModel.openNewScheduleSheet() },
                containerColor = NeonGreen,
                contentColor = OnNeonGreen,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Agenda Cepat",
                        modifier = Modifier.size(22.dp)
                    )
                },
                text = {
                    Text(
                        text = "Tambah Agenda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("weekly_grid_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Multi-column weekly grid timetable
            WeeklyTimetableGrid(
                selectedDate = selectedDate,
                weekSchedules = weekSchedules,
                onEventClick = { item -> viewModel.openEditSheet(item) },
                onEmptySlotClick = { date, hour ->
                    val defaultItem = ScheduleItem(
                        title = "",
                        date = date.toString(),
                        startTime = String.format("%02d:00", hour),
                        endTime = String.format("%02d:00", (hour + 1).coerceAtMost(23)),
                        category = "task"
                    )
                    viewModel.openEditSheet(defaultItem)
                }
            )
        }

        // Inline Event Creation & Editing BottomSheet Modal
        if (editingSchedule != null || isNewModalOpen) {
            ScheduleDetailBottomSheet(
                schedule = editingSchedule,
                defaultDate = selectedDate,
                onDismiss = { viewModel.closeSheet() },
                onSave = { item -> viewModel.saveSchedule(item) },
                onDelete = editingSchedule?.takeIf { it.id != 0L }?.let { itemToDelete ->
                    { viewModel.deleteSchedule(itemToDelete) }
                }
            )
        }
    }
}
