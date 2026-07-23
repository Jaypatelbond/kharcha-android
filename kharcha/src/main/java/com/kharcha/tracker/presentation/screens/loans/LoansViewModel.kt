package com.kharcha.tracker.presentation.screens.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.core.database.dao.LoanDao
import com.kharcha.core.database.entity.LoanEntity
import com.kharcha.core.model.Loan
import com.kharcha.core.domain.usecase.LoanAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoansUiState(
    val activeLoans: List<LoanEntity> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val totalMonthlyEmi: Double = 0.0,
    val cibilScoreEstimate: Int = 750
)

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val loanDao: LoanDao,
    private val analysisUseCase: LoanAnalysisUseCase
) : ViewModel() {

    private val _loans = loanDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<LoansUiState> = _loans.combine(MutableStateFlow(Unit)) { loans, _ ->
        val activeLoans = loans.filter { it.status == "ACTIVE" }
        val loanModels = loans.map {
            Loan(
                id = it.id,
                name = it.name,
                bankName = it.bankName,
                principalAmount = it.principalAmount,
                interestRate = it.interestRate,
                tenureMonths = it.tenureMonths,
                startDate = it.startDate,
                emiAmount = it.emiAmount,
                outstandingBalance = it.outstandingBalance,
                status = it.status,
                type = it.type
            )
        }
        
        val totalEmi = analysisUseCase.calculateTotalMonthlyEmi(loanModels)
        val score = analysisUseCase.simulateCibilScore(loanModels)

        LoansUiState(
            activeLoans = activeLoans, // Keep using activeLoans here properly
            totalOutstanding = activeLoans.sumOf { it.outstandingBalance },
            totalMonthlyEmi = totalEmi,
            cibilScoreEstimate = score
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoansUiState())

    fun addLoan(loan: LoanEntity) {
        viewModelScope.launch {
            loanDao.insert(loan)
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            loanDao.delete(loan)
        }
    }
}
