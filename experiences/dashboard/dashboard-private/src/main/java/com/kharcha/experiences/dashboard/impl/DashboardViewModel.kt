package com.kharcha.experiences.dashboard.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.datastore.KharchaPreferences
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType
import com.kharcha.core.model.CollectionModel
import com.kharcha.core.domain.repository.TransactionRepository
import com.kharcha.core.domain.repository.CollectionRepository
import com.kharcha.core.common.util.DateUtils
import com.kharcha.core.domain.manager.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val collectionRepository: CollectionRepository,
    private val reminderManager: ReminderManager,
    private val kharchaPreferences: KharchaPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    val isReminderEnabled: StateFlow<Boolean> = kharchaPreferences.isReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val reminderHour: StateFlow<Int> = kharchaPreferences.reminderHour
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 20
        )

    val reminderMinute: StateFlow<Int> = kharchaPreferences.reminderMinute
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var allCachedTransactions: List<Transaction> = emptyList()
    private var allCachedCollections: List<CollectionModel> = emptyList()

    init {
        viewModelScope.launch {
            kharchaPreferences.isAdFree.collect {
                _isAdFree.value = it
            }
        }

        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                collectionRepository.getCollections()
            ) { allTxs, cols ->
                allCachedTransactions = allTxs
                allCachedCollections = cols
                recalculateState()
            }.collect {}
        }
    }

    private fun recalculateState() {
        val allTxs = allCachedTransactions
        val currentState = _uiState.value
        val date = currentState.selectedDate
        val selectedCol = currentState.selectedCollection

        val expenseTxs = allTxs.filter { it.type == TransactionType.EXPENSE }
        val totalAllTime = expenseTxs.sumOf { it.amount }

        val txsByCollection = expenseTxs.groupBy { it.collection.ifBlank { "Home Expenses" } }

        // Start from all collections in database
        val registeredNames = allCachedCollections.map { it.name }.toSet()
        val extraNames = txsByCollection.keys.filter { it !in registeredNames }

        val colStats = (allCachedCollections.map { col ->
            val txs = txsByCollection[col.name] ?: emptyList()
            CollectionStat(
                name = col.name,
                totalExpense = txs.sumOf { it.amount },
                count = txs.size,
                isDefault = col.isDefault || col.name.equals("Home Expenses", ignoreCase = true)
            )
        } + extraNames.map { name ->
            val txs = txsByCollection[name] ?: emptyList()
            CollectionStat(
                name = name,
                totalExpense = txs.sumOf { it.amount },
                count = txs.size,
                isDefault = name.equals("Home Expenses", ignoreCase = true)
            )
        }).sortedWith(compareByDescending<CollectionStat> { it.isDefault }.thenByDescending { it.totalExpense })

        val startOfMonth = DateUtils.getStartOfMonth(date)
        val endOfMonth = DateUtils.getEndOfMonth(date)
        val monthlyExpense = expenseTxs.filter { it.date in startOfMonth..endOfMonth }.sumOf { it.amount }

        val startOfDay = DateUtils.getStartOfDay(date)
        val endOfDay = DateUtils.getEndOfDay(date)
        val dayTxs = expenseTxs.filter { it.date in startOfDay..endOfDay }
        val dailyExpense = dayTxs.sumOf { it.amount }

        // Selected collection details & displayed transactions
        val (displayedList, isFiltering, selectedColTotal) = if (!selectedCol.isNullOrBlank() && selectedCol != "All" && selectedCol != "All Books" && selectedCol != "All Collections") {
            val colTxs = allTxs.filter {
                val colName = it.collection.ifBlank { "Home Expenses" }
                colName.equals(selectedCol, ignoreCase = true)
            }
            val colExpense = colTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            Triple(colTxs.sortedByDescending { it.date }, true, colExpense)
        } else {
            val defaultTxs = if (dayTxs.isNotEmpty()) dayTxs.sortedByDescending { it.date } else allTxs.take(30)
            Triple(defaultTxs, false, null)
        }

        _uiState.update {
            it.copy(
                totalKharchaAllTime = totalAllTime,
                monthlyKharcha = monthlyExpense,
                dailyKharcha = dailyExpense,
                totalTransactionsCount = expenseTxs.size,
                collections = colStats,
                selectedCollection = selectedCol,
                selectedCollectionTotal = selectedColTotal,
                currentMonthLabel = DateUtils.formatMonthYear(date),
                selectedDate = date,
                displayedTransactions = displayedList,
                isFilteringByCollection = isFiltering,
                isLoading = false
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        recalculateState()
    }

    fun onSelectCollection(collection: String?) {
        val target = if (collection == "All" || collection == "All Collections" || collection == "All Books") null else collection
        val nextCol = if (_uiState.value.selectedCollection == target) null else target
        _uiState.update { it.copy(selectedCollection = nextCol) }
        recalculateState()
    }

    fun createCollection(name: String, onComplete: ((Result<Unit>) -> Unit)? = null) {
        viewModelScope.launch {
            val res = collectionRepository.addCollection(name)
            onComplete?.invoke(res)
        }
    }

    fun renameCollection(oldName: String, newName: String, onComplete: ((Result<Unit>) -> Unit)? = null) {
        viewModelScope.launch {
            val res = collectionRepository.renameCollection(oldName, newName)
            if (res.isSuccess && _uiState.value.selectedCollection.equals(oldName, ignoreCase = true)) {
                _uiState.update { it.copy(selectedCollection = newName.trim()) }
            }
            onComplete?.invoke(res)
        }
    }

    fun deleteCollection(name: String, onComplete: ((Result<Unit>) -> Unit)? = null) {
        viewModelScope.launch {
            val res = collectionRepository.deleteCollection(name)
            if (res.isSuccess && _uiState.value.selectedCollection.equals(name, ignoreCase = true)) {
                _uiState.update { it.copy(selectedCollection = null) }
            }
            onComplete?.invoke(res)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun scheduleReminder(context: android.content.Context, hour: Int, minute: Int, isEnabled: Boolean) {
        reminderManager.scheduleReminder(context, hour, minute, isEnabled)
        viewModelScope.launch {
            kharchaPreferences.setReminderSettings(isEnabled, hour, minute)
        }
    }

    fun sendTestNotification(context: android.content.Context) {
        reminderManager.sendTestNotification(context)
    }
}
