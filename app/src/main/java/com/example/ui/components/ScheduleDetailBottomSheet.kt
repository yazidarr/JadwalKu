package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.ScheduleCategory
import com.example.data.model.ScheduleItem
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OnNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailBottomSheet(
    schedule: ScheduleItem?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit,
    onDelete: ((ScheduleItem) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEditing = schedule != null

    var title by remember(schedule) { mutableStateOf(schedule?.title ?: "") }
    var description by remember(schedule) { mutableStateOf(schedule?.description ?: "") }

    val initialDateStr = schedule?.date ?: defaultDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    var selectedDateStr by remember(schedule) { mutableStateOf(initialDateStr) }

    var startTime by remember(schedule) { mutableStateOf(schedule?.startTime ?: "09:00") }
    var endTime by remember(schedule) { mutableStateOf(schedule?.endTime ?: "10:00") }

    var selectedCategory by remember(schedule) {
        mutableStateOf(schedule?.categoryEnum ?: ScheduleCategory.TASK)
    }

    var isReminderEnabled by remember(schedule) {
        mutableStateOf(schedule?.isReminderEnabled ?: true)
    }

    var reminderMinutes by remember(schedule) {
        mutableIntStateOf(schedule?.reminderMinutesBefore ?: 15)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DarkBorder, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isEditing) "Edit Agenda" else "Tambah Agenda Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Input
            Text(
                text = "Nama Agenda",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Contoh: Rapat Evaluasi Proyek", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("modal_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Date Quick Selectors
            Text(
                text = "Tanggal Agenda",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val today = LocalDate.now()
            val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val tomorrowStr = today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val lusaStr = today.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateOptionButton(
                    label = "Hari Ini",
                    dateValue = todayStr,
                    isSelected = selectedDateStr == todayStr,
                    onClick = { selectedDateStr = todayStr },
                    modifier = Modifier.weight(1f)
                )
                DateOptionButton(
                    label = "Besok",
                    dateValue = tomorrowStr,
                    isSelected = selectedDateStr == tomorrowStr,
                    onClick = { selectedDateStr = tomorrowStr },
                    modifier = Modifier.weight(1f)
                )
                DateOptionButton(
                    label = "Lusa",
                    dateValue = lusaStr,
                    isSelected = selectedDateStr == lusaStr,
                    onClick = { selectedDateStr = lusaStr },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Selector (Start & End Time)
            Text(
                text = "Rentang Waktu",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Time
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Mulai", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("modal_start_time_input")
                )

                Text("—", color = TextSecondary, fontWeight = FontWeight.Bold)

                // End Time
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Selesai", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("modal_end_time_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Picker
            Text(
                text = "Kategori Warna",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScheduleCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) cat.color.copy(alpha = 0.25f) else DarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) cat.color else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(cat.color, CircleShape)
                            )
                            Text(
                                text = cat.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextWhite else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Settings
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pengingat Real-Time",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnNeonGreen,
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkBorder
                            )
                        )
                    }

                    if (isReminderEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Kirim notifikasi sebelum acara:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val offsets = listOf(
                            0 to "Tepat Waktu",
                            10 to "10 Menit",
                            15 to "15 Menit",
                            30 to "30 Menit",
                            60 to "1 Jam"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            offsets.forEach { (mins, label) ->
                                val isChosen = reminderMinutes == mins
                                Surface(
                                    onClick = { reminderMinutes = mins },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChosen) ElectricBlue.copy(alpha = 0.25f) else DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChosen) ElectricBlue else DarkBorder
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = if (isChosen) TextWhite else TextSecondary,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description / Notes input
            Text(
                text = "Catatan / Tautan Rapat / Lokasi",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Contoh: Google Meet link atau ruang lab 3", color = TextMuted) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("modal_description_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Save & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isEditing && onDelete != null && schedule != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(schedule)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("modal_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        val validTitle = if (title.isBlank()) "Agenda Baru" else title.trim()
                        val updatedItem = ScheduleItem(
                            id = schedule?.id ?: 0L,
                            title = validTitle,
                            description = description.trim(),
                            date = selectedDateStr,
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            category = selectedCategory.id,
                            isCompleted = schedule?.isCompleted ?: false,
                            isReminderEnabled = isReminderEnabled,
                            reminderMinutesBefore = reminderMinutes
                        )
                        onSave(updatedItem)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = OnNeonGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(if (isEditing) 2f else 1f)
                        .height(48.dp)
                        .testTag("modal_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Simpan Perubahan" else "Buat Agenda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DateOptionButton(
    label: String,
    dateValue: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) NeonGreen.copy(alpha = 0.2f) else DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NeonGreen else DarkBorder
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NeonGreen else TextSecondary
            )
        }
    }
}
