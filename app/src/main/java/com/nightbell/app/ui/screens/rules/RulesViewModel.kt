package com.nightbell.app.ui.screens.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightbell.app.NightBellApp
import com.nightbell.app.core.common.Constants
import com.nightbell.app.domain.model.AlarmSettings
import com.nightbell.app.domain.model.AppRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RulesUiState(
    val selectedApp: String = Constants.PACKAGE_FIVERR,
    val fiverrRule: AppRule = AppRule(packageName = Constants.PACKAGE_FIVERR),
    val upworkRule: AppRule = AppRule(packageName = Constants.PACKAGE_UPWORK),
    val alarmSettings: AlarmSettings = AlarmSettings()
) {
    val activeRule: AppRule
        get() = if (selectedApp == Constants.PACKAGE_FIVERR) fiverrRule else upworkRule
}

class RulesViewModel : ViewModel() {

    private val dataStore = NightBellApp.instance.settingsDataStore

    private val _selectedApp = MutableStateFlow(Constants.PACKAGE_FIVERR)
    val selectedApp: StateFlow<String> = _selectedApp.asStateFlow()

    val uiState: StateFlow<RulesUiState> = combine(
        _selectedApp,
        dataStore.fiverrRuleFlow,
        dataStore.upworkRuleFlow,
        dataStore.alarmSettingsFlow
    ) { app, fiverr, upwork, alarm ->
        RulesUiState(
            selectedApp = app,
            fiverrRule = fiverr,
            upworkRule = upwork,
            alarmSettings = alarm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RulesUiState()
    )

    fun selectApp(packageName: String) {
        _selectedApp.value = packageName
    }

    fun toggleEnabled(current: Boolean) {
        viewModelScope.launch {
            dataStore.setAppEnabled(_selectedApp.value, !current)
        }
    }

    fun toggleMessagesOnly(current: Boolean) {
        viewModelScope.launch {
            dataStore.setMessagesOnly(_selectedApp.value, !current)
        }
    }

    fun toggleUseSchedule(current: Boolean) {
        viewModelScope.launch {
            dataStore.setUseSchedule(_selectedApp.value, !current)
        }
    }

    fun addIgnoreWord(word: String) {
        viewModelScope.launch {
            dataStore.addIgnoreWord(_selectedApp.value, word)
        }
    }

    fun removeIgnoreWord(word: String) {
        viewModelScope.launch {
            dataStore.removeIgnoreWord(_selectedApp.value, word)
        }
    }

    fun setTone(toneName: String) {
        viewModelScope.launch {
            dataStore.setAlarmTone(toneName)
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            dataStore.setAlarmVolume(volume)
        }
    }

    fun setRepeat(everyMin: Int, count: Int) {
        viewModelScope.launch {
            dataStore.setRepeatConfig(everyMin, count)
        }
    }
}
