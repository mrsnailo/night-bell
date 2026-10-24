package com.nightbell.app.ui.screens.setup

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.DarkBorder
import com.nightbell.app.ui.theme.DarkSurfaceCard
import com.nightbell.app.ui.theme.PrimaryAmber
import com.nightbell.app.ui.theme.SuccessMint
import com.nightbell.app.ui.theme.TextMuted
import com.nightbell.app.ui.theme.TextPrimary

@Composable
fun SetupChecklistScreen(
    setupViewModel: SetupViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by setupViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        setupViewModel.refreshPermissions(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                color = PrimaryAmber,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Setup Checklist",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Ensure all permissions are active so alarms ring reliably while sleeping.",
            fontSize = 14.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Checklist Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                ChecklistItem(
                    title = "Notification Access",
                    description = "Required to detect incoming Fiverr & Upwork messages",
                    isGranted = state.notificationAccessGranted,
                    onFix = { setupViewModel.openNotificationAccessSettings(context) }
                )

                HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                ChecklistItem(
                    title = "Battery: Unrestricted",
                    description = "Prevents Android from putting NightBell to sleep at night",
                    isGranted = state.batteryOptimizationIgnored,
                    onFix = { setupViewModel.openBatteryOptimizationSettings(context) }
                )

                HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                ChecklistItem(
                    title = "Post Notifications",
                    description = "Displays the persistent foreground ringing banner",
                    isGranted = state.postNotificationsGranted,
                    onFix = { setupViewModel.openAppSettings(context) }
                )

                HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                ChecklistItem(
                    title = "Full-Screen Over Lock Screen",
                    description = "Allows the alarm screen to wake phone over lock screen",
                    isGranted = state.fullScreenIntentGranted,
                    onFix = { setupViewModel.openAppSettings(context) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OEM Guidance Section
        Text(
            text = "OEM BACKGROUND PROTECTION",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        OemGuideCard(
            brand = "Xiaomi / Redmi / POCO (HyperOS / MIUI)",
            tips = "1. Security App → Manage Apps → NightBell → Enable 'Autostart'\n" +
                    "2. Battery Saver → Set to 'No restrictions'\n" +
                    "3. Open Recent Apps → Long-press NightBell → Tap Lock icon"
        )

        Spacer(modifier = Modifier.height(10.dp))

        OemGuideCard(
            brand = "Samsung (One UI)",
            tips = "1. Settings → Apps → NightBell → Battery → Select 'Unrestricted'\n" +
                    "2. Settings → Battery → Background usage limits → Add NightBell to 'Never sleeping apps'"
        )

        Spacer(modifier = Modifier.height(10.dp))

        OemGuideCard(
            brand = "OPPO / Realme / Vivo",
            tips = "1. Settings → Battery → More Settings → App Battery Management → Allow background activity\n" +
                    "2. Settings → App Management → Autostart Apps → Turn ON for NightBell"
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ChecklistItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }

        if (isGranted) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SuccessMint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = SuccessMint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = onFix,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAmber,
                    contentColor = DarkBackground
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Fix", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OemGuideCard(brand: String, tips: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceCard, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = brand,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryAmber
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tips,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
        }
    }
}
