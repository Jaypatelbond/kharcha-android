package com.kharcha.tracker.presentation.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.data.local.dao.RecurringDao
import com.kharcha.tracker.data.local.entity.RecurringTransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringUiState(
    val showDialog: Boolean = false,
    val amount: String = "",
    val note: String = "",
    val type: String = "EXPENSE",
    val frequency: String = "MONTHLY",
    val reminderEnabled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringDao: RecurringDao
) : ViewModel() {

    val recurringTransactions = recurringDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState = _uiState.asStateFlow()

    fun onAddClick() {
        _uiState.update { RecurringUiState(showDialog = true) }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(amount = filtered, error = null) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value, error = null) }
    }

    fun onTypeChange(isExpense: Boolean) {
        _uiState.update { it.copy(type = if (isExpense) "EXPENSE" else "INCOME") }
    }

    fun onFrequencyChange(frequency: String) {
        _uiState.update { it.copy(frequency = frequency) }
    }

    fun onReminderToggle(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun saveRecurringTransaction() {
        val state = _uiState.value
        val amountVal = state.amount.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            _uiState.update { it.copy(error = "Invalid amount") }
            return
        }
        if (state.note.isBlank()) {
            _uiState.update { it.copy(error = "Note cannot be empty") }
            return
        }

        viewModelScope.launch {
            val entity = RecurringTransactionEntity(
                amount = amountVal,
                categoryId = 0, // Default or generic
                note = state.note,
                type = state.type,
                frequency = state.frequency,
                startDate = System.currentTimeMillis(),
                reminderEnabled = state.reminderEnabled,
                // Simple logic: if monthly, next due is 1 month from now.
                // For MVP, we'll set it to today + interval logic later or just let worker handle it.
                // Setting nextDueDate to today so it checks immediately or next run.
                nextDueDate = System.currentTimeMillis()
            )
            recurringDao.insert(entity)
            _uiState.update { it.copy(showDialog = false) }
        }
    }

    fun deleteRecurringTransaction(transaction: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringDao.delete(transaction)
        }
    }
}
