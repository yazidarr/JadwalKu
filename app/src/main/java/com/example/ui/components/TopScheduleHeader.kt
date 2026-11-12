package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.ScheduleCategory
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TopScheduleHeader(
    selectedDate: LocalDate,
    onNavigateDate: (Long) -> Unit,
    onJumpToday: () -> Unit,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = selectedDate == LocalDate.now()
    val localeId = Locale.forLanguageTag("id-ID")
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)
    val formattedDate = selectedDate.format(dayFormatter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        // Top row: App Name / Date title and Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "JadwalKu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.testTag("app_title_text")
                    )

                    if (isToday) {
                        Surface(
                            color = NeonGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(NeonGreen, CircleShape)
                                )
                                Text(
                                    text = "Hari Ini",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = formattedDate,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Navigation Controls & Search
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isToday) {
                    IconButton(
                        onClick = onJumpToday,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("jump_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Lompat ke Hari Ini",
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onNavigateDate(-1) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("prev_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Hari Sebelumnya",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = { onNavigateDate(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("next_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Hari Berikutnya",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onToggleSearch,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Cari Jadwal",
                        tint = if (isSearchActive) NeonGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search Bar (Expanded when active)
        AnimatedVisibility(visible = isSearchActive) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Cari judul agenda atau catatan…", color = TextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Horizontal Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Semua" Filter Chip
            CategoryChipItem(
                label = "Semua",
                color = ElectricBlue,
                isSelected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                testTag = "filter_chip_all"
            )

            ScheduleCategory.entries.forEach { cat ->
                CategoryChipItem(
                    label = cat.displayName,
                    color = cat.color,
                    isSelected = selectedCategory == cat.id,
                    onClick = { onSelectCategory(cat.id) },
                    testTag = "filter_chip_${cat.id}"
                )
            }
        }
    }
}

@Composable
private fun CategoryChipItem(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color.copy(alpha = 0.22f) else DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else DarkBorder
        ),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextWhite else TextSecondary
            )
        }
    }
}
