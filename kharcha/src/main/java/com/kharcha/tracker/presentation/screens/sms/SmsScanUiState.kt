package com.kharcha.tracker.presentation.screens.sms

import com.kharcha.core.model.SmsTransaction

data class SmsScanUiState(
    val pendingTransactions: List<SmsTransaction> = emptyList(),
    val isScanning: Boolean = false,
    val isLoading: Boolean = true,
    val newTransactionsCount: Int? = null,
    val error: String? = null,
    val successMessage: String? = null
)
