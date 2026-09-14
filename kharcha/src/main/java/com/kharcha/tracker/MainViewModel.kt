package com.kharcha.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.datastore.KharchaPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val kharchaPreferences: KharchaPreferences,
    private val splitRepository: com.kharcha.core.domain.repository.SplitRepository
) : ViewModel() {
    val isAdFree: StateFlow<Boolean> = kharchaPreferences.isAdFree
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isOnboardingCompleted: StateFlow<Boolean?> = kharchaPreferences.isOnboardingCompleted
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isAppLockEnabled: StateFlow<Boolean> = kharchaPreferences.isAppLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val appLockPinHash: StateFlow<String> = kharchaPreferences.appLockPinHash
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val isBiometricEnabled: StateFlow<Boolean> = kharchaPreferences.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked

    fun setAppUnlocked(unlocked: Boolean) {
        _isAppUnlocked.value = unlocked
    }

    fun onAppBackgrounded() {
        _isAppUnlocked.value = false
    }

    init {
        viewModelScope.launch {
            splitRepository.ensureUuids()
        }
    }
}
