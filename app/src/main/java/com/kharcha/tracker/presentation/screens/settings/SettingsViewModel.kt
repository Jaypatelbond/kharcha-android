package com.kharcha.tracker.presentation.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.domain.repository.AdFreeRepository
import com.kharcha.tracker.domain.repository.CategoryRepository
import com.kharcha.tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false, // This would ideally come from a preference too
    val adFreeExpiry: Long = 0L,
    val exportMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    @ApplicationContext private val context: Context,
    private val analyticsManager: com.kharcha.tracker.data.analytics.AnalyticsManager,
    private val adFreeRepository: AdFreeRepository,
    private val googleDriveHelper: com.kharcha.tracker.data.remote.GoogleDriveHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            adFreeRepository.adFreeExpiry.collect { expiry ->
                _uiState.update { it.copy(adFreeExpiry = expiry) }
            }
        }
    }

    fun isAdFree(): Boolean {
        return System.currentTimeMillis() < (_uiState.value.adFreeExpiry ?: 0L)
    }

    fun grantAdFreeAccess() {
        viewModelScope.launch {
            adFreeRepository.grantAdFreeAccess()
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
                        val date = com.kharcha.tracker.util.DateUtils.formatDate(t.date)
                        appendLine("$date,${t.type},${t.category.displayName},${t.amount},${t.paymentMode.displayName},\"${t.note}\"")
                    }
                }
                
                val filename = "kharcha_export_${System.currentTimeMillis()}.csv"
                com.kharcha.tracker.util.FileExporter.saveToDownloads(context, filename, "text/csv") { outputStream ->
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
                val file = com.kharcha.tracker.util.PdfExporter.export(context, transactions)
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
                val (transactions, result) = com.kharcha.tracker.util.CsvImporter.parse(inputStream, categories)
                
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

