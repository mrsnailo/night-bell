package com.nightbell.app.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightbell.app.domain.model.AlarmRecord
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.DarkBorder
import com.nightbell.app.ui.theme.DarkSurfaceCard
import com.nightbell.app.ui.theme.PrimaryAmber
import com.nightbell.app.ui.theme.SuccessMint
import com.nightbell.app.ui.theme.TextMuted
import com.nightbell.app.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = viewModel()
) {
    val state by historyViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Text(
            text = "History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "All",
                isSelected = state.filter == HistoryFilter.ALL,
                onClick = { historyViewModel.setFilter(HistoryFilter.ALL) }
            )

            FilterChipItem(
                label = "Fiverr",
                isSelected = state.filter == HistoryFilter.FIVERR,
                onClick = { historyViewModel.setFilter(HistoryFilter.FIVERR) }
            )

            FilterChipItem(
                label = "Upwork",
                isSelected = state.filter == HistoryFilter.UPWORK,
                onClick = { historyViewModel.setFilter(HistoryFilter.UPWORK) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History List Card
        if (state.records.isEmpty()) {
            // Render sample prototype items if no alarms yet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    SampleHistoryRow("Jordan M.", "Tue 2:14 AM · Fiverr", "38 s")
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    SampleHistoryRow("Priya S.", "Tue 12:51 AM · Upwork", "1 m 12 s")
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    SampleHistoryRow("Alex R.", "Sun 3:07 AM · Fiverr", "9 s")
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    SampleHistoryRow("Dana K.", "Sat 11:32 PM · Upwork", "2 m 05 s")
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    SampleHistoryRow("Sam T.", "Fri 1:20 AM · Fiverr", "21 s")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    state.records.forEachIndexed { index, record ->
                        HistoryRecordRow(record)
                        if (index < state.records.size - 1) {
                            HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Time until you stopped the alarm",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) PrimaryAmber else DarkSurfaceCard,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) DarkBackground else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SampleHistoryRow(sender: String, details: String, duration: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = sender, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = details, fontSize = 13.sp, color = TextMuted)
        }
        Text(
            text = duration,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SuccessMint
        )
    }
}

@Composable
private fun HistoryRecordRow(record: AlarmRecord) {
    val appName = if (record.isFiverr) "Fiverr" else "Upwork"
    val dateStr = SimpleDateFormat("EEE h:mm a", Locale.getDefault()).format(Date(record.triggeredAt))
    val details = "$dateStr · $appName"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = record.sender, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = details, fontSize = 13.sp, color = TextMuted)
        }
        Text(
            text = record.formattedDuration,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SuccessMint
        )
    }
}
