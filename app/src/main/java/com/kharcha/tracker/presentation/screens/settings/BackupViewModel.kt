@file:Suppress("DEPRECATION")
package com.kharcha.tracker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.model.File
import com.kharcha.tracker.data.remote.GoogleDriveHelper
import com.kharcha.tracker.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isSignedIn: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isDeleting: Boolean = false,
    val backups: List<File> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showDeleteDialog: File? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val driveHelper: GoogleDriveHelper,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun onSignInSuccess(account: GoogleSignInAccount) {
        driveHelper.initialize(account)
        _uiState.value = _uiState.value.copy(isSignedIn = true)
        fetchBackups()
    }

    fun fetchBackups() {
        viewModelScope.launch {
            try {
                val files = backupRepository.getCloudBackups()
                _uiState.value = _uiState.value.copy(backups = files)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to list backups: ${e.message}")
            }
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackingUp = true, 
                errorMessage = null, 
                successMessage = null
            )
            try {
                backupRepository.performCloudBackup()
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false, 
                    successMessage = "Backup Successful!"
                )
                fetchBackups()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false, 
                    errorMessage = "Backup Failed: ${e.message}"
                )
            }
        }
    }

    fun restore(file: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoring = true, 
                errorMessage = null, 
                successMessage = null
            )
            try {
                backupRepository.restoreFromCloud(file.id)
                _uiState.value = _uiState.value.copy(
                    isRestoring = false, 
                    successMessage = "Restore Successful! Please restart the app."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isRestoring = false, 
                    errorMessage = "Restore Failed: ${e.message}"
                )
            }
        }
    }

    fun showDeleteConfirmation(file: File) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = file)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = null)
    }

    fun deleteBackup(file: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                showDeleteDialog = null,
                errorMessage = null,
                successMessage = null
            )
            try {
                backupRepository.deleteCloudBackup(file.id)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    successMessage = "Backup deleted"
                )
                fetchBackups()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Delete failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
