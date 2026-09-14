package com.kharcha.tracker.presentation.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.datastore.KharchaPreferences
import com.kharcha.core.domain.repository.CategoryRepository
import com.kharcha.core.domain.repository.CollectionRepository
import com.kharcha.core.model.CollectionModel
import com.kharcha.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.kharcha.core.data.util.FileExporter
import com.kharcha.core.data.util.PdfExporter
import com.kharcha.core.data.util.CsvImporter

data class SettingsUiState(
    val isDarkMode: Boolean = false, // This would ideally come from a preference too
    val adFreeExpiry: Long = 0L,
    val exportMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val collectionRepository: CollectionRepository,
    @ApplicationContext private val context: Context,
    private val analyticsManager: com.kharcha.core.data.analytics.AnalyticsManager,
    private val kharchaPreferences: KharchaPreferences,
    private val googleDriveHelper: com.kharcha.core.data.remote.GoogleDriveHelper
) : ViewModel() {

    val collections: StateFlow<List<CollectionModel>> = collectionRepository.getCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCollection(name: String) {
        viewModelScope.launch { collectionRepository.addCollection(name) }
    }

    fun renameCollection(oldName: String, newName: String) {
        viewModelScope.launch { collectionRepository.renameCollection(oldName, newName) }
    }

    fun deleteCollection(name: String) {
        viewModelScope.launch { collectionRepository.deleteCollection(name) }
    }

    val isAppLockEnabled: StateFlow<Boolean> = kharchaPreferences.isAppLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = kharchaPreferences.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun enableAppLock(pin: String) {
        viewModelScope.launch {
            kharchaPreferences.enableAppLock(pin, true)
        }
    }

    fun disableAppLock() {
        viewModelScope.launch {
            kharchaPreferences.disableAppLock()
        }
    }

    fun changePin(newPin: String) {
        viewModelScope.launch {
            kharchaPreferences.updatePin(newPin)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            kharchaPreferences.setBiometricEnabled(enabled)
        }
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            kharchaPreferences.adFreeExpiry.collect { expiry ->
                _uiState.update { it.copy(adFreeExpiry = expiry) }
            }
        }
    }

    fun isAdFree(): Boolean {
        return System.currentTimeMillis() < (_uiState.value.adFreeExpiry ?: 0L)
    }

    fun grantAdFreeAccess() {
        viewModelScope.launch {
            kharchaPreferences.grantAdFreeAccess()
        }
    }

    fun onDarkModeToggle(isDark: Boolean) {
        _uiState.update { it.copy(isDarkMode = isDark) }
    }

    fun exportToCsv() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            analyticsManager.logFeatureUsed("export_csv")
            try {
                val transactions = repository.getAllTransactions().first()
                if (transactions.isEmpty()) {
                    _uiState.update { it.copy(exportMessage = "No transactions to export") }
                    return@launch
                }
                
                val csv = buildString {
                    appendLine("Date,Type,Category,Amount,Payment Mode,Note")
                    transactions.forEach { t ->
                        val date = com.kharcha.core.common.util.DateUtils.formatDate(t.date)
                        appendLine("$date,${t.type},${t.category.displayName},${t.amount},${t.paymentMode.displayName},\"${t.note}\"")
                    }
                }
                
                val filename = "kharcha_export_${System.currentTimeMillis()}.csv"
                FileExporter.saveToDownloads(context, filename, "text/csv") { outputStream ->
                    outputStream.write(csv.toByteArray())
                }
                
                _uiState.update { it.copy(exportMessage = "CSV saved to Downloads ($filename)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Export failed: ${e.message}") }
            }
        }
    }
    
    fun exportToPdf() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            analyticsManager.logFeatureUsed("export_pdf")
            try {
                val transactions = repository.getAllTransactions().first()
                if (transactions.isEmpty()) {
                    _uiState.update { it.copy(exportMessage = "No transactions to export") }
                    return@launch
                }
                val file = PdfExporter.export(context, transactions)
                _uiState.update { it.copy(exportMessage = "PDF saved to Downloads (${file.name})") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "PDF export failed: ${e.message}") }
            }
        }
    }

    fun importCsv(uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            analyticsManager.logFeatureUsed("import_csv")
            _uiState.update { it.copy(exportMessage = "Importing...") }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open file")
                
                val categories = categoryRepository.getAllCategories().first()
                val (transactions, result) = CsvImporter.parse(inputStream, categories)
                
                transactions.forEach { repository.insertTransaction(it) }
                
                val message = if (result.failureCount == 0) {
                    "Successfully imported ${result.successCount} transactions."
                } else {
                    "Imported ${result.successCount}. Failed ${result.failureCount}. Errors: ${result.errors.take(3).joinToString()}"
                }
                
                _uiState.update { it.copy(exportMessage = message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Import failed: ${e.message}") }
            }
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }
}

