package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ScheduleDatabase
import com.example.data.model.ScheduleCategory
import com.example.data.model.ScheduleItem
import com.example.data.repository.ScheduleRepository
import com.example.parser.NaturalLanguageParser
import com.example.parser.ParsedScheduleResult
import com.example.receiver.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class ScheduleViewModel(
    application: Application,
    private val repository: ScheduleRepository
) : AndroidViewModel(application) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.DAY)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _quickInputText = MutableStateFlow("")
    val quickInputText: StateFlow<String> = _quickInputText.asStateFlow()

    private val _parsedQuickInput = MutableStateFlow<ParsedScheduleResult?>(null)
    val parsedQuickInput: StateFlow<ParsedScheduleResult?> = _parsedQuickInput.asStateFlow()

    private val _editingSchedule = MutableStateFlow<ScheduleItem?>(null)
    val editingSchedule: StateFlow<ScheduleItem?> = _editingSchedule.asStateFlow()

    private val _isNewScheduleModalOpen = MutableStateFlow(false)
    val isNewScheduleModalOpen: StateFlow<Boolean> = _isNewScheduleModalOpen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var lastDeletedSchedule: ScheduleItem? = null

    val allSchedules: StateFlow<List<ScheduleItem>> = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Schedules filtered by selected date and active filters
    val currentDaySchedules: StateFlow<List<ScheduleItem>> = combine(
        allSchedules,
        _selectedDate,
        _categoryFilter,
        _searchQuery
    ) { schedules, date, category, query ->
        val dateStr = date.format(dateFormatter)
        schedules.filter { item ->
            val matchesDate = item.date == dateStr
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            matchesDate && matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Schedules for selected week (Monday to Sunday)
    val currentWeekSchedules: StateFlow<Map<LocalDate, List<ScheduleItem>>> = combine(
        allSchedules,
        _selectedDate,
        _categoryFilter
    ) { schedules, date, category ->
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

        val map = mutableMapOf<LocalDate, List<ScheduleItem>>()
        weekDays.forEach { day ->
            val dayStr = day.format(dateFormatter)
            val itemsForDay = schedules.filter {
                it.date == dayStr && (category == null || it.category == category)
            }
            map[day] = itemsForDay
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Schedules for Agenda list (upcoming grouped by date)
    val agendaSchedules: StateFlow<Map<String, List<ScheduleItem>>> = combine(
        allSchedules,
        _categoryFilter,
        _searchQuery
    ) { schedules, category, query ->
        val filtered = schedules.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        filtered.groupBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun navigateDateBy(days: Long) {
        _selectedDate.value = _selectedDate.value.plusDays(days)
    }

    fun jumpToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = if (_categoryFilter.value == category) null else category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun onQuickInputChange(text: String) {
        _quickInputText.value = text
        if (text.isNotBlank() && text.length >= 3) {
            val parsed = NaturalLanguageParser.parse(text, _selectedDate.value)
            _parsedQuickInput.value = parsed
        } else {
            _parsedQuickInput.value = null
        }
    }

    fun submitQuickInput() {
        val parsed = _parsedQuickInput.value ?: return
        val item = ScheduleItem(
            title = parsed.title,
            description = "Dibuat via Input Cepat",
            date = parsed.date.format(dateFormatter),
            startTime = String.format("%02d:%02d", parsed.startTime.hour, parsed.startTime.minute),
            endTime = String.format("%02d:%02d", parsed.endTime.hour, parsed.endTime.minute),
            category = parsed.category.id,
            isCompleted = false,
            isReminderEnabled = parsed.isReminderEnabled,
            reminderMinutesBefore = parsed.reminderMinutesBefore
        )

        viewModelScope.launch {
            val insertedId = repository.insertSchedule(item)
            val insertedItem = item.copy(id = insertedId)
            if (insertedItem.isReminderEnabled) {
                ReminderManager.scheduleReminder(getApplication(), insertedItem)
            }
            _quickInputText.value = ""
            _parsedQuickInput.value = null
            _selectedDate.value = parsed.date
            _snackbarMessage.value = "Agenda ditambahkan: ${item.title}"
        }
    }

    fun toggleComplete(schedule: ScheduleItem) {
        viewModelScope.launch {
            repository.toggleCompleted(schedule)
            val updatedStatus = if (!schedule.isCompleted) "selesai" else "belum selesai"
            _snackbarMessage.value = "Ditandai $updatedStatus: ${schedule.title}"
        }
    }

    fun shiftTime(schedule: ScheduleItem, minutesToAdd: Int) {
        viewModelScope.launch {
            repository.shiftScheduleTime(schedule, minutesToAdd)
            val sign = if (minutesToAdd > 0) "+$minutesToAdd" else "$minutesToAdd"
            _snackbarMessage.value = "Waktu digeser $sign mnt untuk ${schedule.title}"
        }
    }

    fun deleteSchedule(schedule: ScheduleItem) {
        lastDeletedSchedule = schedule
        viewModelScope.launch {
            ReminderManager.cancelReminder(getApplication(), schedule.id)
            repository.deleteSchedule(schedule)
            _snackbarMessage.value = "Agenda dihapus: ${schedule.title}"
        }
    }

    fun undoDelete() {
        val toRestore = lastDeletedSchedule ?: return
        viewModelScope.launch {
            val newId = repository.insertSchedule(toRestore.copy(id = 0L))
            if (toRestore.isReminderEnabled) {
                ReminderManager.scheduleReminder(getApplication(), toRestore.copy(id = newId))
            }
            lastDeletedSchedule = null
            _snackbarMessage.value = "Agenda dipulihkan: ${toRestore.title}"
        }
    }

    fun openEditSheet(schedule: ScheduleItem) {
        _editingSchedule.value = schedule
    }

    fun openNewScheduleSheet() {
        _isNewScheduleModalOpen.value = true
    }

    fun closeSheet() {
        _editingSchedule.value = null
        _isNewScheduleModalOpen.value = false
    }

    fun saveSchedule(item: ScheduleItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                val newId = repository.insertSchedule(item)
                val newItem = item.copy(id = newId)
                if (newItem.isReminderEnabled) {
                    ReminderManager.scheduleReminder(getApplication(), newItem)
                }
                _snackbarMessage.value = "Agenda baru tersimpan!"
            } else {
                repository.updateSchedule(item)
                if (item.isReminderEnabled) {
                    ReminderManager.scheduleReminder(getApplication(), item)
                } else {
                    ReminderManager.cancelReminder(getApplication(), item.id)
                }
                _snackbarMessage.value = "Perubahan agenda tersimpan!"
            }
            closeSheet()
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = ScheduleDatabase.getInstance(application)
                    val repository = ScheduleRepository(db.scheduleDao())
                    return ScheduleViewModel(application, repository) as T
                }
            }
    }
}
