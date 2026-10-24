package com.nightbell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nightbell.app.ui.components.NightBellBottomBar
import com.nightbell.app.ui.navigation.Screen
import com.nightbell.app.ui.screens.history.HistoryScreen
import com.nightbell.app.ui.screens.home.HomeScreen
import com.nightbell.app.ui.screens.rules.RulesScreen
import com.nightbell.app.ui.screens.setup.SetupChecklistScreen
import com.nightbell.app.ui.theme.DarkBackground
import com.nightbell.app.ui.theme.NightBellTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NightBellTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                Scaffold(
                    bottomBar = {
                        if (currentScreen != Screen.Setup) {
                            NightBellBottomBar(
                                currentScreen = currentScreen,
                                onNavigate = { screen ->
                                    currentScreen = screen
                                }
                            )
                        }
                    },
                    containerColor = DarkBackground
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(bottom = if (currentScreen != Screen.Setup) paddingValues.calculateBottomPadding() else paddingValues.calculateBottomPadding())
                    ) {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen(
                                onNavigateToSetup = { currentScreen = Screen.Setup }
                            )
                            Screen.Rules -> RulesScreen()
                            Screen.History -> HistoryScreen()
                            Screen.Setup -> SetupChecklistScreen(
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
