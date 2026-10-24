package com.nightbell.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightbell.app.core.common.Constants
import com.nightbell.app.ui.theme.AmberMutedBg
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.DarkSurfaceCard
import com.nightbell.app.ui.theme.PrimaryAmber
import com.nightbell.app.ui.theme.SuccessMint
import com.nightbell.app.ui.theme.TextMuted
import com.nightbell.app.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToSetup: () -> Unit = {}
) {
    val state by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // Screen Title & Checklist shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NightBell",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Setup",
                color = PrimaryAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onNavigateToSetup() }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Bell Circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(AmberMutedBg, CircleShape)
                        .border(2.dp, PrimaryAmber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(40.dp)) {
                        val stroke = 1.8.dp.toPx()
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.25f, size.height * 0.65f)
                            lineTo(size.width * 0.25f, size.height * 0.45f)
                            cubicTo(
                                size.width * 0.25f, size.height * 0.2f,
                                size.width * 0.75f, size.height * 0.2f,
                                size.width * 0.75f, size.height * 0.45f
                            )
                            lineTo(size.width * 0.75f, size.height * 0.65f)
                            lineTo(size.width * 0.85f, size.height * 0.75f)
                            lineTo(size.width * 0.15f, size.height * 0.75f)
                            close()
                        }
                        drawPath(path, PrimaryAmber, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                        drawArc(
                            color = PrimaryAmber,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height * 0.78f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.2f, size.height * 0.14f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (state.isListening) "Listening" else "Paused",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = state.scheduleSubtitle,
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = state.healthText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SuccessMint
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Watching
        Text(
            text = "WATCHING",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fiverr Card
        AppWatchCard(
            title = "Fiverr",
            subtitle = if (state.fiverrRule.messagesOnly) "Client messages only" else "All notifications",
            isEnabled = state.fiverrRule.enabled,
            onToggle = { homeViewModel.toggleApp(Constants.PACKAGE_FIVERR, state.fiverrRule.enabled) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Upwork Card
        AppWatchCard(
            title = "Upwork",
            subtitle = if (state.upworkRule.messagesOnly) "Client messages only" else "All notifications",
            isEnabled = state.upworkRule.enabled,
            onToggle = { homeViewModel.toggleApp(Constants.PACKAGE_UPWORK, state.upworkRule.enabled) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Last Alarm Summary Card
        val last = state.lastAlarm
        val lastApp = if (last?.isFiverr == true) "Fiverr" else "Upwork"
        val lastSender = last?.sender ?: "Jordan M."
        val lastTime = if (last != null) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(last.triggeredAt))
        } else {
            "2:14 AM"
        }
        val lastDuration = if (last != null) "Stopped in ${last.formattedDuration}" else "Stopped in 38 s"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LAST ALARM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$lastApp · $lastSender",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = lastTime,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Text(
                        text = lastDuration,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Test Alarm Button
        Button(
            onClick = { homeViewModel.triggerTestAlarm(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryAmber,
                contentColor = DarkBackground
            )
        ) {
            Text(
                text = "Test alarm",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(100.dp)) // padding for bottom bar
    }
}

@Composable
private fun AppWatchCard(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = PrimaryAmber,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkBackground
                )
            )
        }
    }
}
