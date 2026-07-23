package com.kharcha.tracker.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.domain.model.Transaction
import com.kharcha.tracker.domain.model.TransactionType
import com.kharcha.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: TransactionType? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                _uiState.update { state ->
                    state.copy(
                        transactions = transactions,
                        filteredTransactions = applyFilters(transactions, state.searchQuery, state.selectedTypeFilter),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTransactions = applyFilters(state.transactions, query, state.selectedTypeFilter)
            )
        }
    }

    fun onTypeFilterChange(type: TransactionType?) {
        _uiState.update { state ->
            state.copy(
                selectedTypeFilter = type,
                filteredTransactions = applyFilters(state.transactions, state.searchQuery, type)
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    private fun applyFilters(
        transactions: List<Transaction>,
        query: String,
        typeFilter: TransactionType?
    ): List<Transaction> {
        var result = transactions
        if (query.isNotBlank()) {
            result = result.filter {
                it.note.contains(query, ignoreCase = true)
                        || it.category.displayName.contains(query, ignoreCase = true)
            }
        }
        if (typeFilter != null) {
            result = result.filter { it.type == typeFilter }
        }
        return result
    }
}
