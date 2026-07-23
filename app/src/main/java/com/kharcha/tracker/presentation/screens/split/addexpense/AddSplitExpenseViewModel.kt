package com.kharcha.tracker.presentation.screens.split.addexpense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.domain.model.SplitExpenseShare
import com.kharcha.tracker.domain.model.SplitMember
import com.kharcha.tracker.domain.model.SplitType
import com.kharcha.tracker.domain.repository.SplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseShareUiState(
    val memberId: Long,
    val memberName: String,
    val isIncluded: Boolean = true
)

data class AddSplitExpenseState(
    val description: String = "",
    val amount: String = "",
    val paidByMemberId: Long? = null,
    val splitType: SplitType = SplitType.EQUAL,
    val members: List<SplitMember> = emptyList(),
    val shares: List<ExpenseShareUiState> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddSplitExpenseViewModel @Inject constructor(
    private val repository: SplitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(AddSplitExpenseState())
    val uiState: StateFlow<AddSplitExpenseState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Fetch group details to get members
                val groupDetails = repository.getGroupDetails(groupId).first()
                val members = groupDetails.members
                
                _uiState.update { 
                    it.copy(
                        members = members,
                        shares = members.map { member -> 
                            ExpenseShareUiState(member.id, member.name) 
                        },
                        isLoading = false,
                        paidByMemberId = members.firstOrNull()?.id // Default to first member
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load members: ${e.message}") }
            }
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onAmountChange(value: String) {
        // Validation: only numbers and one decimal point
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(amount = value) }
        }
    }

    fun onPaidByChange(memberId: Long) {
        _uiState.update { it.copy(paidByMemberId = memberId) }
    }

    fun toggleShareInclusion(memberId: Long) {
        _uiState.update { state ->
            val updatedShares = state.shares.map { 
                if (it.memberId == memberId) it.copy(isIncluded = !it.isIncluded) else it 
            }
            state.copy(shares = updatedShares)
        }
    }

    fun saveExpense() {
        val state = uiState.value
        val amount = state.amount.toDoubleOrNull()
        
        if (state.description.isBlank()) {
            _uiState.update { it.copy(error = "Enter description") }
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Enter valid amount") }
            return
        }
        if (state.paidByMemberId == null) {
            _uiState.update { it.copy(error = "Select who paid") }
            return
        }

        val includedShares = state.shares.filter { it.isIncluded }
        if (includedShares.isEmpty()) {
            _uiState.update { it.copy(error = "Select at least one person to split with") }
            return
        }

        // Calculate split amounts (only EQUAL implemented for now)
        val shareAmount = amount / includedShares.size
        
        val finalShares = includedShares.map { 
            SplitExpenseShare(
                expenseId = 0, // Will be set by repo/db
                memberId = it.memberId,
                shareAmount = shareAmount
            )
        }

        viewModelScope.launch {
            try {
                repository.addExpense(
                    groupId = groupId,
                    description = state.description,
                    amount = amount,
                    paidByMemberId = state.paidByMemberId,
                    splitType = state.splitType.name,
                    shares = finalShares
                )
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save: ${e.message}") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
