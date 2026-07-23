package com.kharcha.tracker.presentation.screens.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.SmsTransaction
import com.kharcha.core.domain.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsScanViewModel @Inject constructor(
    private val repository: SmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsScanUiState())
    val uiState: StateFlow<SmsScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPendingTransactions()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { transactions ->
                    _uiState.update { 
                        it.copy(
                            pendingTransactions = transactions,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun scanSms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            try {
                val count = repository.scanMessages()
                _uiState.update { 
                    it.copy(
                        isScanning = false,
                        newTransactionsCount = count
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isScanning = false,
                        error = "Scan failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun approveTransaction(transaction: SmsTransaction) {
        viewModelScope.launch {
            try {
                repository.approveTransaction(transaction)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Approval failed: ${e.message}") }
            }
        }
    }

    fun approveAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            try {
                val count = repository.approveAllPending()
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        newTransactionsCount = count,
                        successMessage = "Added $count transactions as expenses"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        error = "Bulk approve failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun rejectTransaction(transaction: SmsTransaction) {
        viewModelScope.launch {
            try {
                repository.rejectTransaction(transaction)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Rejection failed: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(newTransactionsCount = null, error = null, successMessage = null) }
    }
}
