package com.kharcha.tracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kharcha.tracker.presentation.theme.TealPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarWeekView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // We maintain a "current week start" state to navigate weeks
    // But for simplicity and better UX, let's show a month view or a scrollable week view? 
    // The request image shows a month selector "February - 2023" and arrows. 
    // Below that are days Mo, Tu, We... and dates.
    // Let's implement exactly that: A row for Month/Year nav, and a row for the days of that month/week.
    
    // Actually, a full month view might take too much space if we show all weeks.
    // The image shows a single row of dates. This suggests a "Week View" that can change weeks.
    // Or it could be a horizontal pager of weeks.
    
    // Let's start with a state for the currently displayed week's start date.
    var currentWeekStart by remember { mutableStateOf(selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)) }
    
    // Update current week if selected date changes externally/initially
    LaunchedEffect(selectedDate) {
        val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
        // Only update if the selected date is clearly outside the current view? 
        // Or just always sync? Let's sync if it's far off, but for now simple sync.
        if (startOfWeek != currentWeekStart) {
             currentWeekStart = startOfWeek
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Subtle contrast
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Month - Year and Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Previous Week"
                    )
                }

                val headerDate = currentWeekStart.plusDays(3) // Mid-week determines the label? Or start? 
                // Usually the month of the majority of days.
                Text(
                    text = headerDate.format(DateTimeFormatter.ofPattern("MMMM - yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Next Week"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Days Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround // Better distribution
            ) {
                val weekDays = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
                
                weekDays.forEach { date ->
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDateSelected(date) }
                            .background(if (isSelected) TealPrimary else Color.Transparent)
                            .padding(vertical = 8.dp, horizontal = 6.dp) // Reduced padding
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (isToday && !isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TealPrimary)
                            )
                        } else {
                             Spacer(modifier = Modifier.height(4.dp))
                             // Invisible spacer to keep alignment
                             Box(modifier = Modifier.size(4.dp))
                        }
                    }
                }
            }
        }
    }
}
