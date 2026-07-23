package com.kharcha.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.datastore.KharchaPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    kharchaPreferences: KharchaPreferences,
    private val splitRepository: com.kharcha.core.domain.repository.SplitRepository
) : ViewModel() {
    val isAdFree: StateFlow<Boolean> = kharchaPreferences.isAdFree
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isOnboardingCompleted: StateFlow<Boolean?> = kharchaPreferences.isOnboardingCompleted
        .map { it } // explicit type help if needed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            splitRepository.ensureUuids()
        }
    }
}
