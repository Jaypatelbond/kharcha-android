@file:Suppress("DEPRECATION")
package com.kharcha.tracker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.kharcha.core.data.remote.GoogleDriveHelper
import com.kharcha.core.data.repository.BackupRepository
import com.kharcha.core.model.BackupItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String? = null,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isDeleting: Boolean = false,
    val backups: List<BackupItem> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showDeleteDialog: BackupItem? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val driveHelper: GoogleDriveHelper,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        fetchBackups()
    }

    fun onSignInSuccess(account: GoogleSignInAccount) {
        driveHelper.initialize(account)
        _uiState.value = _uiState.value.copy(
            isSignedIn = true,
            accountEmail = account.email
        )
        fetchBackups()
    }

    fun onSignOut() {
        _uiState.value = _uiState.value.copy(
            isSignedIn = false,
            accountEmail = null
        )
        fetchBackups()
    }

    fun fetchBackups() {
        viewModelScope.launch {
            try {
                val files = backupRepository.getAllBackups(includeCloud = _uiState.value.isSignedIn)
                _uiState.value = _uiState.value.copy(backups = files)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to list backups: ${e.message}")
            }
        }
    }

    fun backupNow(toCloud: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackingUp = true, 
                errorMessage = null, 
                successMessage = null
            )
            try {
                if (toCloud && _uiState.value.isSignedIn) {
                    backupRepository.performCloudBackup()
                    _uiState.value = _uiState.value.copy(
                        isBackingUp = false, 
                        successMessage = "Cloud & Local Backup Successful!"
                    )
                } else {
                    backupRepository.performLocalBackup()
                    _uiState.value = _uiState.value.copy(
                        isBackingUp = false, 
                        successMessage = "Device Backup Created Successfully!"
                    )
                }
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

    fun restore(item: BackupItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoring = true, 
                errorMessage = null, 
                successMessage = null
            )
            try {
                backupRepository.restore(item)
                _uiState.value = _uiState.value.copy(
                    isRestoring = false, 
                    successMessage = "Restore Successful! Please restart app to reload data."
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

    fun showDeleteConfirmation(item: BackupItem) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = item)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = null)
    }

    fun deleteBackup(item: BackupItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                showDeleteDialog = null,
                errorMessage = null,
                successMessage = null
            )
            try {
                backupRepository.deleteBackup(item)
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
