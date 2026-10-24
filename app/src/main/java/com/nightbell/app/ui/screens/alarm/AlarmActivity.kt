package com.nightbell.app.ui.screens.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nightbell.app.ui.theme.NightBellTheme

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NightBellTheme {
                // To be implemented in Phase 4
            }
        }
    }
}
