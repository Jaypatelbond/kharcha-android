package com.kharcha.tracker.presentation.screens.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.tracker.data.local.dao.CreditCardDao
import com.kharcha.tracker.data.local.dao.IncomeDao
import com.kharcha.tracker.data.local.dao.LoanDao
import com.kharcha.tracker.data.local.dao.RecurringDao
import com.kharcha.tracker.data.local.dao.TransactionDao
import com.kharcha.tracker.data.local.entity.CreditCardEntity
import com.kharcha.tracker.data.local.entity.IncomeProfileEntity
import com.kharcha.tracker.domain.model.*
import com.kharcha.tracker.domain.usecase.CreditCardTip
import com.kharcha.tracker.domain.usecase.LoanAnalysisUseCase
import com.kharcha.tracker.domain.usecase.SavingsOpportunity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtSimulationViewModel @Inject constructor(
    private val loanDao: LoanDao,
    private val creditCardDao: CreditCardDao,
    private val incomeDao: IncomeDao,
    private val recurringDao: RecurringDao,
    private val transactionDao: TransactionDao,
    private val analysisUseCase: LoanAnalysisUseCase
) : ViewModel() {

    // ── User-adjustable scenario parameters ─────────────────────────
    private val _extraMonthlyPayment = MutableStateFlow(0.0)
    private val _lumpSumPayment = MutableStateFlow(0.0)
    private val _strategy = MutableStateFlow(DebtStrategy.AVALANCHE)
    private val _selectedCardIndex = MutableStateFlow(0)

    // ── Income input state (auto-populated from transaction history) ─
    private val _monthlySalary = MutableStateFlow("")
    private val _otherIncome = MutableStateFlow("")
    private val _monthlyExpenses = MutableStateFlow("")
    val monthlySalary: StateFlow<String> = _monthlySalary
    val otherIncome: StateFlow<String> = _otherIncome
    val monthlyExpenses: StateFlow<String> = _monthlyExpenses

    // Track whether auto-population has happened
    private var hasAutoPopulated = false

    // ── Credit Card input state ─────────────────────────────────────
    private val _showAddCardDialog = MutableStateFlow(false)
    val showAddCardDialog: StateFlow<Boolean> = _showAddCardDialog

    init {
        // Auto-populate income from actual transaction data on first load
        autoPopulateFromTransactions()
    }

    /**
     * Calculates income and expenses from last 30 days of transaction data
     * and recurring transactions, then auto-populates the input fields.
     */
    private fun autoPopulateFromTransactions() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            // Combine transaction-based income/expenses with recurring data
            combine(
                transactionDao.getTotalByTypeAndDateRange("INCOME", thirtyDaysAgo, now),
                transactionDao.getTotalByTypeAndDateRange("EXPENSE", thirtyDaysAgo, now),
                recurringDao.getActive(),
                incomeDao.getLatest()
            ) { txnIncome, txnExpense, recurringItems, savedIncome ->
                AutoPopData(txnIncome, txnExpense, recurringItems, savedIncome)
            }.first().let { data ->
                // Recurring totals
                val recurringIncome = data.recurringItems
                    .filter { it.type == "INCOME" && it.isActive }
                    .sumOf { it.amount }
                val recurringExpense = data.recurringItems
                    .filter { it.type == "EXPENSE" && it.isActive }
                    .sumOf { it.amount }

                // Use the larger of: saved income profile, transaction data, or recurring data
                val bestSalary = maxOf(
                    data.savedIncome?.monthlySalary ?: 0.0,
                    data.txnIncome,
                    recurringIncome
                )
                val bestExpense = maxOf(
                    data.savedIncome?.monthlyExpenses ?: 0.0,
                    data.txnExpense,
                    recurringExpense
                )
                val bestOther = data.savedIncome?.otherIncome ?: 0.0

                if (!hasAutoPopulated) {
                    hasAutoPopulated = true
                    if (bestSalary > 0) _monthlySalary.value = bestSalary.toLong().toString()
                    _otherIncome.value = bestOther.toLong().toString()
                    if (bestExpense > 0) _monthlyExpenses.value = bestExpense.toLong().toString()

                    // Auto-save income profile if we calculated from transactions and none saved
                    if (data.savedIncome == null && bestSalary > 0) {
                        incomeDao.insert(IncomeProfileEntity(
                            monthlySalary = bestSalary,
                            otherIncome = bestOther,
                            monthlyExpenses = bestExpense
                        ))
                    }
                }
            }
        }
    }

    // ── Combined UI State ───────────────────────────────────────────
    val uiState: StateFlow<SimulationUiState> = combine(
        loanDao.getActiveLoans(),
        creditCardDao.getActiveCards(),
        incomeDao.getLatest(),
        recurringDao.getActive(),
        _strategy
    ) { loansEntities, cardEntities, incomeEntity, recurringEntities, strategy ->

        val loans = loansEntities.map { e ->
            Loan(e.id, e.name, e.bankName, e.principalAmount, e.interestRate,
                e.tenureMonths, e.startDate, e.emiAmount, e.outstandingBalance,
                e.status, e.type)
        }

        val creditCards = cardEntities.map { e ->
            CreditCard(e.id, e.cardName, e.bankName, e.creditLimit,
                e.outstandingBalance, e.apr, e.minPaymentPercent,
                e.billingDate, e.dueDate, e.statementBalance, e.status)
        }

        val income = incomeEntity?.let {
            IncomeProfile(it.id, it.monthlySalary, it.otherIncome, it.monthlyExpenses, it.lastUpdated)
        }

        val recurringExpenses = recurringEntities
            .filter { it.type == "EXPENSE" && it.isActive }
            .sumOf { it.amount }

        // Income analysis
        val incomeAnalysis = income?.let {
            analysisUseCase.analyzeIncome(it, loans, creditCards, recurringExpenses)
        }

        // CC cycle info (for selected card)
        val selectedCard = creditCards.getOrNull(_selectedCardIndex.value)
        val cycleInfo = selectedCard?.let { analysisUseCase.simulateCreditCardCycle(it) }

        // Min payment trap (for selected card)
        val minPaymentTrap = selectedCard?.let { analysisUseCase.simulateMinPaymentTrap(it) }

        // Debt freedom simulation
        val scenario = SimulationScenario(
            strategy = strategy,
            extraMonthlyPayment = _extraMonthlyPayment.value,
            lumpSumPayment = _lumpSumPayment.value,
            lumpSumMonth = 1
        )
        val debtFreedom = analysisUseCase.simulateDebtFreedom(loans, creditCards, income, scenario)

        // Action plan
        val actionPlan = analysisUseCase.generateActionPlan(income, loans, creditCards)

        // Strategy display
        val totalEmi = loans.sumOf { it.emiAmount }
        val totalCardDebt = creditCards.sumOf { it.outstandingBalance }

        SimulationUiState(
            hasData = loans.isNotEmpty() || creditCards.isNotEmpty(),
            hasIncome = income != null,
            loans = loans,
            creditCards = creditCards,
            income = income,
            totalEmi = totalEmi,
            totalCardDebt = totalCardDebt,
            strategy = strategy,
            extraMonthlyPayment = _extraMonthlyPayment.value,
            lumpSumPayment = _lumpSumPayment.value,
            incomeAnalysis = incomeAnalysis,
            creditCardCycleInfo = cycleInfo,
            minPaymentTrap = minPaymentTrap,
            debtFreedom = debtFreedom,
            actionPlan = actionPlan,
            selectedCardIndex = _selectedCardIndex.value,
            selectedCard = selectedCard,
            savingsOpportunities = analysisUseCase.calculateSavingsOpportunities(loans),
            creditCardTips = analysisUseCase.generateCreditCardTips(loans)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SimulationUiState())

    // ── Actions ─────────────────────────────────────────────────────

    fun setStrategy(strategy: DebtStrategy) { _strategy.value = strategy }
    fun setExtraPayment(amount: Double) { _extraMonthlyPayment.value = amount }
    fun setLumpSum(amount: Double) { _lumpSumPayment.value = amount }
    fun selectCard(index: Int) { _selectedCardIndex.value = index }
    fun toggleAddCardDialog() { _showAddCardDialog.value = !_showAddCardDialog.value }

    fun updateMonthlySalary(value: String) { _monthlySalary.value = value }
    fun updateOtherIncome(value: String) { _otherIncome.value = value }
    fun updateMonthlyExpenses(value: String) { _monthlyExpenses.value = value }

    fun saveIncome() {
        viewModelScope.launch {
            val salary = _monthlySalary.value.toDoubleOrNull() ?: return@launch
            val other = _otherIncome.value.toDoubleOrNull() ?: 0.0
            val expenses = _monthlyExpenses.value.toDoubleOrNull() ?: 0.0
            incomeDao.insert(IncomeProfileEntity(
                monthlySalary = salary,
                otherIncome = other,
                monthlyExpenses = expenses
            ))
        }
    }

    fun addCreditCard(
        cardName: String, bankName: String, creditLimit: Double,
        outstandingBalance: Double, apr: Double, minPaymentPercent: Double,
        billingDate: Int, dueDate: Int, statementBalance: Double
    ) {
        viewModelScope.launch {
            creditCardDao.insert(CreditCardEntity(
                cardName = cardName, bankName = bankName,
                creditLimit = creditLimit, outstandingBalance = outstandingBalance,
                apr = apr, minPaymentPercent = minPaymentPercent,
                billingDate = billingDate, dueDate = dueDate,
                statementBalance = statementBalance, status = "ACTIVE"
            ))
            _showAddCardDialog.value = false
        }
    }

    fun deleteCreditCard(card: CreditCard) {
        viewModelScope.launch {
            creditCardDao.delete(CreditCardEntity(
                id = card.id, cardName = card.cardName, bankName = card.bankName,
                creditLimit = card.creditLimit, outstandingBalance = card.outstandingBalance,
                apr = card.apr, minPaymentPercent = card.minPaymentPercent,
                billingDate = card.billingDate, dueDate = card.dueDate,
                statementBalance = card.statementBalance, status = card.status
            ))
        }
    }
}

// ── Helper data class for auto-population ────────────────────────────
private data class AutoPopData(
    val txnIncome: Double,
    val txnExpense: Double,
    val recurringItems: List<com.kharcha.tracker.data.local.entity.RecurringTransactionEntity>,
    val savedIncome: IncomeProfileEntity?
)

// ── UI State ─────────────────────────────────────────────────────────
data class SimulationUiState(
    val hasData: Boolean = false,
    val hasIncome: Boolean = false,
    val loans: List<Loan> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val income: IncomeProfile? = null,
    val totalEmi: Double = 0.0,
    val totalCardDebt: Double = 0.0,
    val strategy: DebtStrategy = DebtStrategy.AVALANCHE,
    val extraMonthlyPayment: Double = 0.0,
    val lumpSumPayment: Double = 0.0,
    val incomeAnalysis: IncomeAnalysis? = null,
    val creditCardCycleInfo: CreditCardCycleInfo? = null,
    val minPaymentTrap: MinPaymentTrapResult? = null,
    val debtFreedom: DebtFreedomResult? = null,
    val actionPlan: List<ActionItem> = emptyList(),
    val selectedCardIndex: Int = 0,
    val selectedCard: CreditCard? = null,
    val savingsOpportunities: List<SavingsOpportunity> = emptyList(),
    val creditCardTips: List<CreditCardTip> = emptyList()
)
