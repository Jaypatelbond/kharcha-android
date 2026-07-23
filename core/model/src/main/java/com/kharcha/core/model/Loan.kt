package com.kharcha.core.model

data class Loan(
    val id: Long = 0,
    val name: String,
    val bankName: String,
    val principalAmount: Double,
    val interestRate: Double, // Annual %
    val tenureMonths: Int,
    val startDate: Long,
    val emiAmount: Double,
    val outstandingBalance: Double,
    val status: String, // ACTIVE, CLOSED
    val type: String
)

data class EmiScheduleItem(
    val month: Int,
    val principalPaid: Double,
    val interestPaid: Double,
    val balance: Double,
    val date: Long
)

enum class DebtStrategy {
    AVALANCHE, // Highest Interest Rate First
    SNOWBALL   // Lowest Balance First
}

// ── Credit Card Model ───────────────────────────────────────────────
data class CreditCard(
    val id: Long = 0,
    val cardName: String,
    val bankName: String,
    val creditLimit: Double,
    val outstandingBalance: Double,
    val apr: Double,              // Annual % (e.g., 42.0 for revolving)
    val minPaymentPercent: Double, // e.g., 5.0
    val billingDate: Int,          // Day of month (1–28)
    val dueDate: Int,              // Day of month
    val statementBalance: Double,
    val status: String             // ACTIVE, CLOSED
)

// ── Income Profile ──────────────────────────────────────────────────
data class IncomeProfile(
    val id: Long = 0,
    val monthlySalary: Double,
    val otherIncome: Double,       // Freelance, rent, etc.
    val monthlyExpenses: Double,   // Essential living expenses
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalMonthlyIncome: Double get() = monthlySalary + otherIncome
    val surplus: Double get() = totalMonthlyIncome - monthlyExpenses
}

// ── Simulation Scenario (user-configurable) ─────────────────────────
data class SimulationScenario(
    val strategy: DebtStrategy = DebtStrategy.AVALANCHE,
    val extraMonthlyPayment: Double = 0.0,
    val lumpSumPayment: Double = 0.0,
    val lumpSumTargetLoanId: Long? = null, // Which loan gets the lump sum
    val lumpSumMonth: Int = 1              // When to apply lump sum
)

// ── Full Simulation Result ──────────────────────────────────────────
data class DebtFreedomResult(
    val timeline: List<DebtFreedomMonth>,
    val totalMonths: Int,
    val totalInterestPaid: Double,
    val interestSavedVsNormal: Double,
    val monthsSavedVsNormal: Int,
    val debtFreeDate: String,
    val loanPayoffOrder: List<LoanPayoffInfo>
)

data class DebtFreedomMonth(
    val month: Int,
    val totalOutstanding: Double,
    val totalCreditCardDebt: Double,
    val totalLoanDebt: Double,
    val interestPaid: Double,
    val principalPaid: Double,
    val debtsRemaining: Int,
    val debtsPaidOff: List<String> // Names of debts paid off this month
)

data class LoanPayoffInfo(
    val name: String,
    val bankName: String,
    val originalBalance: Double,
    val totalInterestPaid: Double,
    val monthsToPay: Int,
    val payoffMonth: Int
)

// ── Min Payment Trap ────────────────────────────────────────────────
data class MinPaymentTrapResult(
    val timeline: List<MinPaymentMonth>,
    val totalMonthsToPayoff: Int,
    val totalInterestPaid: Double,
    val totalAmountPaid: Double,      // Principal + interest
    val originalBalance: Double,
    val interestMultiplier: Double     // How many times the original balance in interest
)

data class MinPaymentMonth(
    val month: Int,
    val payment: Double,
    val interestCharged: Double,
    val principalPaid: Double,
    val remainingBalance: Double
)

// ── Credit Card Cycle Info ──────────────────────────────────────────
data class CreditCardCycleInfo(
    val interestFreeDays: Int,
    val optimalPurchaseWindowStart: Int, // Day of month
    val optimalPurchaseWindowEnd: Int,
    val nextBillingDate: String,
    val nextDueDate: String,
    val dailyInterestRate: Double,
    val monthlyInterestCost: Double,   // If balance carried forward
    val cycleBreakdown: List<CyclePeriod>
)

data class CyclePeriod(
    val label: String,      // "Interest-Free", "Grace Period", "Interest Accruing"
    val startDay: Int,
    val endDay: Int,
    val description: String
)

// ── Income Analysis ─────────────────────────────────────────────────
data class IncomeAnalysis(
    val debtToIncomeRatio: Double,     // As percentage (e.g., 45.0)
    val affordabilityScore: Int,        // 0–100
    val monthlySurplus: Double,
    val monthlyDeficit: Double,
    val maxAffordableEmi: Double,
    val riskLevel: String,             // LOW, MODERATE, HIGH, CRITICAL
    val suggestions: List<IncomeSuggestion>
)

data class IncomeSuggestion(
    val title: String,
    val description: String,
    val icon: String,
    val priority: Int,  // 1 = highest
    val savingsAmount: Double = 0.0
)

// ── Smart Action Plan ───────────────────────────────────────────────
data class ActionItem(
    val rank: Int,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,       // "IMMEDIATE", "SHORT_TERM", "LONG_TERM"
    val potentialSaving: Double,
    val effort: String          // "Easy", "Medium", "Hard"
)
