package com.nightbell.app.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.TextPrimary

@Composable
fun SetupChecklistScreen(
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Setup Checklist", color = TextPrimary)
    }
}
