package com.nightbell.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightbell.app.NightBellApp
import com.nightbell.app.core.common.Constants
import com.nightbell.app.domain.model.AlarmRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistoryFilter { ALL, FIVERR, UPWORK }

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val records: List<AlarmRecord> = emptyList()
)

class HistoryViewModel : ViewModel() {

    private val historyRepo = NightBellApp.instance.alarmHistoryRepository

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        _filter,
        historyRepo.allEventsFlow
    ) { currentFilter, allRecords ->
        val filtered = when (currentFilter) {
            HistoryFilter.ALL -> allRecords
            HistoryFilter.FIVERR -> allRecords.filter { it.packageName == Constants.PACKAGE_FIVERR }
            HistoryFilter.UPWORK -> allRecords.filter { it.packageName == Constants.PACKAGE_UPWORK }
        }
        HistoryUiState(filter = currentFilter, records = filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }
}
