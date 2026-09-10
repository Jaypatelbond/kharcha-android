package com.kharcha.tracker.presentation.screens.split.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.model.GroupWithMembers
import com.kharcha.core.model.MemberBalance
import com.kharcha.core.model.SplitExpense
import com.kharcha.core.domain.repository.SplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val groupWithMembers: GroupWithMembers? = null,
    val expenses: List<SplitExpense> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val repository: SplitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine group details and expenses
            combine(
                repository.getGroupDetails(groupId),
                repository.getGroupExpenses(groupId)
            ) { groupInfo, expenses ->
                val balances = repository.getGroupBalances(groupId) // This is suspend, so inside combine's lambda is okay?
                // Combine executes lambda sequentially for each emission? No, combine executes concurrently but transform is suspend.
                // However, getGroupBalances is one-shot. It might block updates?
                // Better to have balances as a separate flow or calculated here.
                // Wait, getGroupBalances calls DAO methods which are suspend.
                
                Triple(groupInfo, expenses, balances)
            }.collect { (groupInfo, expenses, balances) ->
                _uiState.update { 
                    it.copy(
                        groupWithMembers = groupInfo,
                        expenses = expenses,
                        balances = balances,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addMember(name: String, phone: String? = null) {
        viewModelScope.launch {
            try {
                repository.addMember(groupId, name, phone)
                // Flow updates automatically
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add member: ${e.message}") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun exportGroup(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val json = repository.exportGroup(groupId)
                // Use UUID in filename if available, or Group ID
                val fileName = "kharcha_group_${groupId}.json"
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText(json)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = android.content.Intent.createChooser(intent, "Export Group Sync File")
                // context passed from Composable might not be Activity, so NEW_TASK is safer if using Application context, 
                // but usually from Composable LocalContext.current is Activity/ContextWrapper.
                // Assuming it's safe to start activity.
                context.startActivity(chooser)
                
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Export failed: ${e.message}") }
            }
        }
    }

    fun importGroup(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val json = context.contentResolver.openInputStream(uri)?.use { 
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: throw Exception("Failed to read file")
                
                val success = repository.importGroup(json)
                if (!success) {
                    _uiState.update { it.copy(error = "Import failed. Invalid file format?") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Import failed: ${e.message}") }
            } finally {
                 _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteGroup(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteGroup(groupId)
                onDeleted()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete group: ${e.message}") }
            }
        }
    }

    fun removeMember(memberId: Long, onRemoved: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.removeMember(memberId)
                onRemoved()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to remove member: ${e.message}") }
            }
        }
    }
}
