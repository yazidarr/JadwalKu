package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AgendaListView(
    groupedSchedules: Map<String, List<ScheduleItem>>,
    onCardClick: (ScheduleItem) -> Unit,
    onToggleComplete: (ScheduleItem) -> Unit,
    onShiftTime: (ScheduleItem, Int) -> Unit,
    onDelete: (ScheduleItem) -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val tomorrowStr = today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val lusaStr = today.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val localeId = Locale.forLanguageTag("id-ID")
    val allItems = groupedSchedules.values.flatten()
    val totalCount = allItems.size
    val completedCount = allItems.count { it.isCompleted }
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("agenda_list_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stats Summary Card
        item {
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                imageVector = Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Progres Agenda",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Text(
                            text = "$completedCount dari $totalCount selesai",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonGreen,
                        trackColor = DarkSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Geser ke kanan untuk tandai selesai, geser ke kiri untuk hapus.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (groupedSchedules.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tidak ada agenda yang cocok",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gunakan kolom input cepat di atas untuk menambahkan agenda baru.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onAddNew,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = OnNeonGreen
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Agenda Baru", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Grouped by Date sections
            groupedSchedules.forEach { (dateStr, itemsForDate) ->
                val dateLabel = when (dateStr) {
                    todayStr -> "Hari Ini"
                    tomorrowStr -> "Besok"
                    lusaStr -> "Lusa"
                    else -> {
                        try {
                            val parsedDate = LocalDate.parse(dateStr)
                            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", localeId)
                            parsedDate.format(formatter)
                        } catch (_: Exception) {
                            dateStr
                        }
                    }
                }

                item(key = "header_$dateStr") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (dateStr == todayStr) NeonGreen else ElectricBlue,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = dateLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dateStr == todayStr) NeonGreen else TextWhite
                            )
                        }

                        Text(
                            text = "${itemsForDate.size} kegiatan",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                items(itemsForDate, key = { it.id }) { item ->
                    ScheduleCard(
                        item = item,
                        onClick = { onCardClick(item) },
                        onToggleComplete = { onToggleComplete(item) },
                        onShiftTime = { mins -> onShiftTime(item, mins) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}
