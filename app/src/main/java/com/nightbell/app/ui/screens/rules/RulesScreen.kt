package com.nightbell.app.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightbell.app.core.common.Constants
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.DarkBorder
import com.nightbell.app.ui.theme.DarkSurfaceCard
import com.nightbell.app.ui.theme.DashedBorder
import com.nightbell.app.ui.theme.PrimaryAmber
import com.nightbell.app.ui.theme.SliderTrack
import com.nightbell.app.ui.theme.TextBody
import com.nightbell.app.ui.theme.TextMuted
import com.nightbell.app.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    rulesViewModel: RulesViewModel = viewModel()
) {
    val state by rulesViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showAddWordDialog by remember { mutableStateOf(false) }
    var newWordText by remember { mutableStateOf("") }

    var showToneDialog by remember { mutableStateOf(false) }

    val activeRule = state.activeRule
    val isFiverr = state.selectedApp == Constants.PACKAGE_FIVERR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Text(
            text = "Rules",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented App Selector: Fiverr | Upwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(
                            if (isFiverr) PrimaryAmber else DarkSurfaceCard,
                            RoundedCornerShape(11.dp)
                        )
                        .clickable { rulesViewModel.selectApp(Constants.PACKAGE_FIVERR) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fiverr",
                        color = if (isFiverr) DarkBackground else TextMuted,
                        fontSize = 15.sp,
                        fontWeight = if (isFiverr) FontWeight.Bold else FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(
                            if (!isFiverr) PrimaryAmber else DarkSurfaceCard,
                            RoundedCornerShape(11.dp)
                        )
                        .clickable { rulesViewModel.selectApp(Constants.PACKAGE_UPWORK) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Upwork",
                        color = if (!isFiverr) DarkBackground else TextMuted,
                        fontSize = 15.sp,
                        fontWeight = if (!isFiverr) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rules Toggle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column {
                RuleToggleRow(
                    label = "Enabled",
                    checked = activeRule.enabled,
                    onCheckedChange = { rulesViewModel.toggleEnabled(activeRule.enabled) }
                )
                HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                RuleToggleRow(
                    label = "Messages only",
                    checked = activeRule.messagesOnly,
                    onCheckedChange = { rulesViewModel.toggleMessagesOnly(activeRule.messagesOnly) }
                )
                HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                RuleToggleRow(
                    label = "Only in quiet hours",
                    checked = activeRule.useSchedule,
                    onCheckedChange = { rulesViewModel.toggleUseSchedule(activeRule.useSchedule) }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Ignore Words Section
        Text(
            text = "IGNORE WORDS",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activeRule.blockKeywords.forEach { word ->
                Box(
                    modifier = Modifier
                        .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                        .clickable { rulesViewModel.removeIgnoreWord(word) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "$word  ×",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .border(1.dp, DashedBorder, RoundedCornerShape(20.dp))
                    .clickable { showAddWordDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Add word",
                    color = PrimaryAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Alarm Sound Section
        Text(
            text = "ALARM SOUND",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        val currentToneLabel = when (state.alarmSettings.toneUri) {
            Constants.TONE_DIGITAL_URGENCY -> "Digital urgency"
            Constants.TONE_EMERGENCY_BEACON -> "Emergency beacon"
            else -> "Rising siren"
        }

        val volumePercent = (state.alarmSettings.volume * 100).toInt()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                // Tone Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showToneDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tone", fontSize = 15.sp, color = TextPrimary)
                    Text(text = currentToneLabel, fontSize = 15.sp, color = PrimaryAmber, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume Row & Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Volume", fontSize = 15.sp, color = TextPrimary)
                    Text(text = "$volumePercent%", fontSize = 15.sp, color = TextMuted)
                }

                Slider(
                    value = state.alarmSettings.volume,
                    onValueChange = { rulesViewModel.setVolume(it) },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = TextPrimary,
                        activeTrackColor = PrimaryAmber,
                        inactiveTrackColor = SliderTrack
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Repeat Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Repeat if unread", fontSize = 15.sp, color = TextPrimary)
                    Text(text = "every 5 min, 5×", fontSize = 15.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Add Word Dialog
    if (showAddWordDialog) {
        AlertDialog(
            onDismissRequest = { showAddWordDialog = false },
            title = { Text(text = "Add Ignore Word", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newWordText,
                    onValueChange = { newWordText = it },
                    placeholder = { Text("e.g. promo, coupon") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWordText.isNotBlank()) {
                            rulesViewModel.addIgnoreWord(newWordText)
                            newWordText = ""
                        }
                        showAddWordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = DarkBackground)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWordDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    // Tone Picker Dialog
    if (showToneDialog) {
        AlertDialog(
            onDismissRequest = { showToneDialog = false },
            title = { Text(text = "Choose Alarm Tone", color = TextPrimary) },
            text = {
                Column {
                    ToneChoiceItem("Rising siren", isSelected = state.alarmSettings.toneUri == Constants.TONE_RISING_SIREN) {
                        rulesViewModel.setTone(Constants.TONE_RISING_SIREN)
                        showToneDialog = false
                    }
                    ToneChoiceItem("Digital urgency", isSelected = state.alarmSettings.toneUri == Constants.TONE_DIGITAL_URGENCY) {
                        rulesViewModel.setTone(Constants.TONE_DIGITAL_URGENCY)
                        showToneDialog = false
                    }
                    ToneChoiceItem("Emergency beacon", isSelected = state.alarmSettings.toneUri == Constants.TONE_EMERGENCY_BEACON) {
                        rulesViewModel.setTone(Constants.TONE_EMERGENCY_BEACON)
                        showToneDialog = false
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showToneDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }
}

@Composable
private fun ToneChoiceItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = if (isSelected) PrimaryAmber else TextPrimary, fontSize = 16.sp)
        if (isSelected) {
            Text(text = "✓", color = PrimaryAmber, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RuleToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = TextPrimary)

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = PrimaryAmber,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkBackground
            )
        )
    }
}
