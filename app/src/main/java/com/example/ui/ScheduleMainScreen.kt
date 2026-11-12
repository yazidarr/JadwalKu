package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ScheduleItem
import com.example.ui.components.QuickInputBar
import com.example.ui.components.ScheduleDetailBottomSheet
import com.example.ui.components.TopScheduleHeader
import com.example.ui.components.ViewModeSwitcher
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OnNeonGreen
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.ui.viewmodel.ViewMode
import com.example.ui.views.AgendaListView
import com.example.ui.views.DayView
import com.example.ui.views.MonthView
import com.example.ui.views.WeekView

@Composable
fun ScheduleMainScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    val quickInputText by viewModel.quickInputText.collectAsState()
    val parsedQuickInput by viewModel.parsedQuickInput.collectAsState()

    val editingSchedule by viewModel.editingSchedule.collectAsState()
    val isNewScheduleModalOpen by viewModel.isNewScheduleModalOpen.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val currentDaySchedules by viewModel.currentDaySchedules.collectAsState()
    val currentWeekSchedules by viewModel.currentWeekSchedules.collectAsState()
    val agendaSchedules by viewModel.agendaSchedules.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages with Undo support
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (msg.startsWith("Agenda dihapus")) "Urungkan" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
            viewModel.clearSnackbar()
        }
    }

    // Back handler
    BackHandler(enabled = isSearchActive || editingSchedule != null || isNewScheduleModalOpen) {
        when {
            editingSchedule != null || isNewScheduleModalOpen -> viewModel.closeSheet()
            isSearchActive -> viewModel.toggleSearch()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewScheduleSheet() },
                containerColor = NeonGreen,
                contentColor = OnNeonGreen,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_schedule_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Agenda",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with Date title, Nav arrows, Today jump, Search, and Category filters
            TopScheduleHeader(
                selectedDate = selectedDate,
                onNavigateDate = { days -> viewModel.navigateDateBy(days) },
                onJumpToday = { viewModel.jumpToToday() },
                selectedCategory = categoryFilter,
                onSelectCategory = { cat -> viewModel.setCategoryFilter(cat) },
                searchQuery = searchQuery,
                onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                isSearchActive = isSearchActive,
                onToggleSearch = { viewModel.toggleSearch() }
            )

            // View Mode Switcher: Harian, Mingguan, Bulanan, Agenda
            ViewModeSwitcher(
                currentMode = viewMode,
                onModeSelected = { mode -> viewModel.setViewMode(mode) }
            )

            // Natural Language Quick Input Bar
            QuickInputBar(
                text = quickInputText,
                onTextChange = { txt -> viewModel.onQuickInputChange(txt) },
                parsedResult = parsedQuickInput,
                onSubmit = { viewModel.submitQuickInput() }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Body Area based on selected ViewMode
            Box(modifier = Modifier.weight(1f)) {
                when (viewMode) {
                    ViewMode.DAY -> {
                        DayView(
                            selectedDate = selectedDate,
                            schedules = currentDaySchedules,
                            onCardClick = { item -> viewModel.openEditSheet(item) },
                            onToggleComplete = { item -> viewModel.toggleComplete(item) },
                            onShiftTime = { item, mins -> viewModel.shiftTime(item, mins) },
                            onDelete = { item -> viewModel.deleteSchedule(item) },
                            onAddAtHour = { hour ->
                                val defaultItem = ScheduleItem(
                                    title = "",
                                    date = selectedDate.toString(),
                                    startTime = String.format("%02d:00", hour),
                                    endTime = String.format("%02d:00", (hour + 1).coerceAtMost(23)),
                                    category = categoryFilter ?: "task"
                                )
                                viewModel.openEditSheet(defaultItem)
                            }
                        )
                    }

                    ViewMode.WEEK -> {
                        WeekView(
                            selectedDate = selectedDate,
                            weekSchedules = currentWeekSchedules,
                            onDateSelect = { date -> viewModel.setSelectedDate(date) },
                            onCardClick = { item -> viewModel.openEditSheet(item) },
                            onToggleComplete = { item -> viewModel.toggleComplete(item) },
                            onShiftTime = { item, mins -> viewModel.shiftTime(item, mins) },
                            onDelete = { item -> viewModel.deleteSchedule(item) },
                            onAddNew = { viewModel.openNewScheduleSheet() },
                            onAddAtSlot = { date, hour ->
                                val defaultItem = ScheduleItem(
                                    title = "",
                                    date = date.toString(),
                                    startTime = String.format("%02d:00", hour),
                                    endTime = String.format("%02d:00", (hour + 1).coerceAtMost(23)),
                                    category = categoryFilter ?: "task"
                                )
                                viewModel.openEditSheet(defaultItem)
                            }
                        )
                    }

                    ViewMode.MONTH -> {
                        MonthView(
                            selectedDate = selectedDate,
                            allSchedules = allSchedules,
                            onDateSelect = { date -> viewModel.setSelectedDate(date) },
                            onCardClick = { item -> viewModel.openEditSheet(item) },
                            onToggleComplete = { item -> viewModel.toggleComplete(item) },
                            onShiftTime = { item, mins -> viewModel.shiftTime(item, mins) },
                            onDelete = { item -> viewModel.deleteSchedule(item) },
                            onAddNew = { viewModel.openNewScheduleSheet() }
                        )
                    }

                    ViewMode.AGENDA -> {
                        AgendaListView(
                            groupedSchedules = agendaSchedules,
                            onCardClick = { item -> viewModel.openEditSheet(item) },
                            onToggleComplete = { item -> viewModel.toggleComplete(item) },
                            onShiftTime = { item, mins -> viewModel.shiftTime(item, mins) },
                            onDelete = { item -> viewModel.deleteSchedule(item) },
                            onAddNew = { viewModel.openNewScheduleSheet() }
                        )
                    }
                }
            }
        }

        // Inline Edit / New Schedule Bottom Sheet
        if (editingSchedule != null || isNewScheduleModalOpen) {
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
