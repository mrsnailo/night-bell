package com.nightbell.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Rules : Screen("rules", "Rules")
    data object History : Screen("history", "History")
    data object Setup : Screen("setup", "Setup Checklist")
}
