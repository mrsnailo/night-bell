package com.nightbell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.NightBellTheme
import com.nightbell.app.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NightBellTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NightBell",
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
