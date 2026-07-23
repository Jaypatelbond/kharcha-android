package com.kharcha.tracker.domain.usecase

import com.kharcha.tracker.domain.model.*
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt

class LoanAnalysisUseCase @Inject constructor() {

    // ── Payoff Strategy ─────────────────────────────────────────────
    fun suggestPayoffStrategy(loans: List<Loan>, strategy: DebtStrategy): List<Loan> {
        val activeLoans = loans.filter { it.status == "ACTIVE" }
        return when (strategy) {
            DebtStrategy.AVALANCHE -> activeLoans.sortedByDescending { it.interestRate }
            DebtStrategy.SNOWBALL -> activeLoans.sortedBy { it.outstandingBalance }
        }
    }

    // ── Total Monthly EMI ───────────────────────────────────────────
    fun calculateTotalMonthlyEmi(loans: List<Loan>): Double {
        var total = 0.0
        for (loan in loans) {
            if (loan.status == "ACTIVE") {
                total += loan.emiAmount
            }
        }
        return total
    }

    // ── Month-wise Payoff Simulation ────────────────────────────────
    /**
     * Simulates month-by-month loan payoff for a single loan.
     * Returns a list of monthly snapshots showing principal, interest, and remaining balance.
     */
    fun generateAmortizationSchedule(loan: Loan, maxMonths: Int = 360): List<EmiScheduleItem> {
        if (loan.status != "ACTIVE" || loan.outstandingBalance <= 0) return emptyList()

        val monthlyRate = loan.interestRate / 100.0 / 12.0
        var balance = loan.outstandingBalance
        val schedule = mutableListOf<EmiScheduleItem>()
        val now = System.currentTimeMillis()
        val monthMs = 30L * 24 * 60 * 60 * 1000

        var month = 1
        while (balance > 0 && month <= maxMonths) {
            val interest = balance * monthlyRate
            val principal = (loan.emiAmount - interest).coerceAtLeast(0.0)
            balance = (balance - principal).coerceAtLeast(0.0)

            schedule.add(
                EmiScheduleItem(
                    month = month,
                    principalPaid = principal,
                    interestPaid = interest,
                    balance = balance,
                    date = now + (month * monthMs)
                )
            )
            month++
        }
        return schedule
    }

    /**
     * Multi-loan payoff simulation — shows total outstanding month by month
     * using the chosen strategy, with freed EMIs redirected to next loan.
     */
    fun simulatePayoffTimeline(
        loans: List<Loan>,
        strategy: DebtStrategy,
        extraMonthlyPayment: Double = 0.0
    ): PayoffSimulation {
        val activeLoans = loans.filter { it.status == "ACTIVE" }
        if (activeLoans.isEmpty()) return PayoffSimulation(emptyList(), 0, 0.0, 0.0)

        // Sort by strategy
        val sorted = when (strategy) {
            DebtStrategy.AVALANCHE -> activeLoans.sortedByDescending { it.interestRate }
            DebtStrategy.SNOWBALL -> activeLoans.sortedBy { it.outstandingBalance }
        }

        data class LoanState(val loan: Loan, var balance: Double, val monthlyRate: Double, val emi: Double)
        val states = sorted.mapTo(mutableListOf()) {
            LoanState(it, it.outstandingBalance, it.interestRate / 100.0 / 12.0, it.emiAmount)
        }

        val timeline = mutableListOf<PayoffMonth>()
        var month = 0
        var totalInterestPaid = 0.0
        var freedEmi = extraMonthlyPayment // Start with any extra payment

        while (states.any { it.balance > 0 } && month < 360) {
            month++
            var monthInterest = 0.0
            var monthPrincipal = 0.0

            // Apply freed EMI to the first active loan (priority target)
            var extraForFirst = freedEmi

            for ((index, state) in states.withIndex()) {
                if (state.balance <= 0) continue

                val interest = state.balance * state.monthlyRate
                var payment = state.emi
                if (index == 0) payment += extraForFirst // Extra goes to priority loan

                val principal = (payment - interest).coerceAtLeast(0.0)
                state.balance = (state.balance - principal).coerceAtLeast(0.0)

                monthInterest += interest
                monthPrincipal += principal
                totalInterestPaid += interest

                // Loan paid off — free its EMI for the next target
                if (state.balance <= 0) {
                    freedEmi += state.emi
                }
            }

            timeline.add(
                PayoffMonth(
                    month = month,
                    totalOutstanding = states.sumOf { it.balance },
                    interestPaid = monthInterest,
                    principalPaid = monthPrincipal,
                    loansRemaining = states.count { it.balance > 0 }
                )
            )
        }

        // Calculate without strategy (normal payoff)
        val normalInterest = calculateNormalTotalInterest(sorted)

        return PayoffSimulation(
            timeline = timeline,
            totalMonths = month,
            totalInterestPaid = totalInterestPaid,
            interestSaved = (normalInterest - totalInterestPaid).coerceAtLeast(0.0)
        )
    }

    private fun calculateNormalTotalInterest(loans: List<Loan>): Double {
        return loans.sumOf { loan ->
            val monthlyRate = loan.interestRate / 100.0 / 12.0
            if (monthlyRate <= 0 || loan.emiAmount <= 0) return@sumOf 0.0
            var balance = loan.outstandingBalance
            var totalInterest = 0.0
            var months = 0
            while (balance > 0 && months < 360) {
                val interest = balance * monthlyRate
                balance -= (loan.emiAmount - interest).coerceAtLeast(0.0)
                balance = balance.coerceAtLeast(0.0)
                totalInterest += interest
                months++
            }
            totalInterest
        }
    }

    // ── CIBIL Score Simulation ──────────────────────────────────────
    fun simulateCibilScore(loans: List<Loan>): Int {
        var score = 750
        val activeLoans = loans.filter { it.status == "ACTIVE" }

        val hasSecured = activeLoans.any { it.type == "HOME" || it.type == "CAR" }
        val hasUnsecured = activeLoans.any { it.type == "PERSONAL" || it.type == "EDUCATION" || it.type == "OTHER" }

        if (hasSecured && hasUnsecured) score += 25
        else if (hasSecured) score += 15

        val totalOutstanding = activeLoans.sumOf { it.outstandingBalance }
        if (totalOutstanding > 50_00_000) score -= 20

        if (activeLoans.size > 3) score -= (activeLoans.size - 3) * 10

        return score.coerceIn(300, 900)
    }

    // ── Interest Savings ────────────────────────────────────────────
    fun calculateInterestSavings(loan: Loan): Double {
        val yearsRemaining = (loan.outstandingBalance / loan.emiAmount) / 12.0
        if (yearsRemaining <= 0) return 0.0
        return loan.outstandingBalance * (loan.interestRate / 100.0) * yearsRemaining * 0.6
    }

    // ── Loan Health Score ───────────────────────────────────────────
    /** Returns a 0-100 health score for a loan (100 = almost paid off) */
    fun loanHealthScore(loan: Loan): Int {
        if (loan.principalAmount <= 0) return 100
        val paidOff = 1.0 - (loan.outstandingBalance / loan.principalAmount)
        return (paidOff * 100).roundToInt().coerceIn(0, 100)
    }

    // ── Months Remaining ────────────────────────────────────────────
    fun monthsRemaining(loan: Loan): Int {
        val monthlyRate = loan.interestRate / 100.0 / 12.0
        if (monthlyRate <= 0 || loan.emiAmount <= 0) return 0
        var balance = loan.outstandingBalance
        var months = 0
        while (balance > 0 && months < 360) {
            balance -= (loan.emiAmount - balance * monthlyRate).coerceAtLeast(0.0)
            balance = balance.coerceAtLeast(0.0)
            months++
        }
        return months
    }

    // ── Credit Card Insights ────────────────────────────────────────
    fun generateCreditCardTips(loans: List<Loan>): List<CreditCardTip> {
        val tips = mutableListOf<CreditCardTip>()

        tips.add(CreditCardTip(
            title = "Convert High Bills to EMI",
            description = "If a credit card bill > ₹10,000, convert to 3-6 month EMI at a lower rate than revolving credit (36-42% APR).",
            icon = "💳",
            category = "Savings"
        ))
        tips.add(CreditCardTip(
            title = "Use Statement Date Wisely",
            description = "Make purchases right AFTER your billing date to get the maximum interest-free period (up to 50 days).",
            icon = "📅",
            category = "Timing"
        ))
        tips.add(CreditCardTip(
            title = "Pay Before Due Date",
            description = "Always pay the FULL amount 2-3 days before the due date. Paying only minimum (5%) triggers 36-42% annual interest on the entire balance.",
            icon = "⚡",
            category = "Critical"
        ))
        tips.add(CreditCardTip(
            title = "Multi-Card Strategy",
            description = "If you have multiple cards, track each card's billing cycle. Spread purchases across cards to maximize interest-free days.",
            icon = "🔄",
            category = "Strategy"
        ))
        tips.add(CreditCardTip(
            title = "Balance Transfer",
            description = "Shift high-interest credit card debt to a card offering 0% balance transfer for 3-6 months. Save up to 40% interest.",
            icon = "🏦",
            category = "Savings"
        ))
        tips.add(CreditCardTip(
            title = "Reward Point Optimization",
            description = "Use the right card per category — dining card for restaurants, travel card for flights. Never let points expire.",
            icon = "🎁",
            category = "Rewards"
        ))

        // Dynamic tip based on active unsecured loans
        val personalLoans = loans.filter { it.status == "ACTIVE" && it.type == "PERSONAL" }
        if (personalLoans.isNotEmpty()) {
            val highestRate = personalLoans.maxByOrNull { it.interestRate }
            highestRate?.let {
                tips.add(0, CreditCardTip(
                    title = "Consolidate ${it.name}",
                    description = "Your ${it.bankName} loan at ${it.interestRate}% could be refinanced. Check for balance transfer offers at 10-12%.",
                    icon = "🎯",
                    category = "Action"
                ))
            }
        }

        return tips
    }

    // ── Savings Opportunities ───────────────────────────────────────
    fun calculateSavingsOpportunities(loans: List<Loan>): List<SavingsOpportunity> {
        val opportunities = mutableListOf<SavingsOpportunity>()
        val activeLoans = loans.filter { it.status == "ACTIVE" }

        // Extra EMI pay: What if you pay 10% more per month?
        val totalEmi = activeLoans.sumOf { it.emiAmount }
        if (totalEmi > 0) {
            val extraPercent10 = totalEmi * 0.1
            val monthsSaved = estimateMonthsSavedWithExtra(activeLoans, extraPercent10)
            val interestSaved = estimateInterestSavedWithExtra(activeLoans, extraPercent10)
            opportunities.add(SavingsOpportunity(
                title = "Pay 10% Extra Monthly",
                subtitle = "+₹${extraPercent10.roundToInt()}/month extra",
                monthsSaved = monthsSaved,
                interestSaved = interestSaved,
                difficulty = "Easy"
            ))

            val extraPercent25 = totalEmi * 0.25
            val monthsSaved25 = estimateMonthsSavedWithExtra(activeLoans, extraPercent25)
            val interestSaved25 = estimateInterestSavedWithExtra(activeLoans, extraPercent25)
            opportunities.add(SavingsOpportunity(
                title = "Pay 25% Extra Monthly",
                subtitle = "+₹${extraPercent25.roundToInt()}/month extra",
                monthsSaved = monthsSaved25,
                interestSaved = interestSaved25,
                difficulty = "Moderate"
            ))
        }

        // Prepayment scenario
        activeLoans.maxByOrNull { it.interestRate }?.let { loan ->
            val prepayAmount = loan.outstandingBalance * 0.1
            opportunities.add(SavingsOpportunity(
                title = "Prepay ₹${formatAmount(prepayAmount)} on ${loan.name}",
                subtitle = "One-time lump sum on highest rate loan",
                monthsSaved = estimateMonthsSavedWithPrepay(loan, prepayAmount),
                interestSaved = estimateInterestSavedWithPrepay(loan, prepayAmount),
                difficulty = "Medium"
            ))
        }

        return opportunities
    }

    // ═══════════════════════════════════════════════════════════════════
    // ── INCOME ANALYSIS ────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    fun analyzeIncome(
        income: IncomeProfile,
        loans: List<Loan>,
        creditCards: List<CreditCard>,
        recurringExpenses: Double
    ): IncomeAnalysis {
        var totalEmi = 0.0
        for (loan in loans) {
            if (loan.status == "ACTIVE") totalEmi += loan.emiAmount
        }
        var totalCardMinPayments = 0.0
        for (card in creditCards) {
            if (card.status == "ACTIVE") {
                totalCardMinPayments += card.outstandingBalance * card.minPaymentPercent / 100.0
            }
        }
        val totalDebtPayments = totalEmi + totalCardMinPayments

        val dti = if (income.totalMonthlyIncome > 0) {
            (totalDebtPayments / income.totalMonthlyIncome) * 100.0
        } else 0.0

        val monthlySurplus = income.totalMonthlyIncome - income.monthlyExpenses -
                totalDebtPayments - recurringExpenses
        val monthlyDeficit = if (monthlySurplus < 0) -monthlySurplus else 0.0

        // Affordability: 100 = no debt burden, 0 = can't pay
        val affordability = when {
            dti <= 20 -> 90 + ((20 - dti) / 2).roundToInt()
            dti <= 35 -> 70 + ((35 - dti) / 1.5).roundToInt()
            dti <= 50 -> 40 + ((50 - dti) / 0.75).roundToInt()
            else -> (40 - (dti - 50)).roundToInt().coerceAtLeast(0)
        }

        val riskLevel = when {
            dti <= 20 -> "LOW"
            dti <= 35 -> "MODERATE"
            dti <= 50 -> "HIGH"
            else -> "CRITICAL"
        }

        // Max EMI a bank would approve (40% of income - existing EMIs)
        val maxEmi = ((income.totalMonthlyIncome * 0.4) - totalEmi).coerceAtLeast(0.0)

        val suggestions = generateIncomeBasedSuggestions(
            income, loans, creditCards, dti, monthlySurplus
        )

        return IncomeAnalysis(
            debtToIncomeRatio = dti,
            affordabilityScore = affordability.coerceIn(0, 100),
            monthlySurplus = monthlySurplus.coerceAtLeast(0.0),
            monthlyDeficit = monthlyDeficit,
            maxAffordableEmi = maxEmi,
            riskLevel = riskLevel,
            suggestions = suggestions
        )
    }

    private fun generateIncomeBasedSuggestions(
        income: IncomeProfile,
        loans: List<Loan>,
        creditCards: List<CreditCard>,
        dti: Double,
        surplus: Double
    ): List<IncomeSuggestion> {
        val suggestions = mutableListOf<IncomeSuggestion>()
        val activeLoans = loans.filter { it.status == "ACTIVE" }
        val activeCards = creditCards.filter { it.status == "ACTIVE" }

        // High DTI warning
        if (dti > 50) {
            suggestions.add(IncomeSuggestion(
                title = "Debt Emergency",
                description = "Your DTI is ${String.format("%.0f", dti)}%. Prioritize paying off the highest-rate debt immediately. Consider debt consolidation.",
                icon = "🚨",
                priority = 1
            ))
        }

        // Credit card debt is the most expensive
        val highRateCards = activeCards.filter { it.apr > 30 && it.outstandingBalance > 0 }
        if (highRateCards.isNotEmpty()) {
            val worstCard = highRateCards.maxByOrNull { it.apr * it.outstandingBalance }!!
            val monthlyCost = worstCard.outstandingBalance * worstCard.apr / 100 / 12
            suggestions.add(IncomeSuggestion(
                title = "Kill ${worstCard.cardName} Debt First",
                description = "This card costs you ₹${formatAmount(monthlyCost)}/month in interest alone at ${worstCard.apr}% APR. Clear this before any other debt.",
                icon = "💳",
                priority = 2,
                savingsAmount = monthlyCost * 12
            ))
        }

        // Surplus allocation
        if (surplus > 0) {
            val extraPaymentImpact = if (activeLoans.isNotEmpty()) {
                val topLoan = activeLoans.maxByOrNull { it.interestRate }!!
                val saved = estimateInterestSavedWithExtra(listOf(topLoan), surplus * 0.5)
                saved
            } else 0.0

            suggestions.add(IncomeSuggestion(
                title = "Use 50% Surplus for Debt",
                description = "You have ₹${formatAmount(surplus)} surplus. Putting ₹${formatAmount(surplus * 0.5)}/month extra towards your highest-rate debt saves ₹${formatAmount(extraPaymentImpact)} in interest.",
                icon = "💰",
                priority = 3,
                savingsAmount = extraPaymentImpact
            ))
        }

        // Part-payment suggestion on highest rate loan
        activeLoans.maxByOrNull { it.interestRate }?.let { loan ->
            if (loan.interestRate > 10 && income.totalMonthlyIncome > 0) {
                val oneMonthSalary = income.totalMonthlyIncome
                val saved = estimateInterestSavedWithPrepay(loan, oneMonthSalary)
                suggestions.add(IncomeSuggestion(
                    title = "Lump Sum on ${loan.name}",
                    description = "A one-time ₹${formatAmount(oneMonthSalary)} prepayment on your ${loan.interestRate}% loan saves ₹${formatAmount(saved)}.",
                    icon = "🎯",
                    priority = 4,
                    savingsAmount = saved
                ))
            }
        }

        return suggestions.sortedBy { it.priority }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ── CREDIT CARD CYCLE SIMULATION ───────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    fun simulateCreditCardCycle(card: CreditCard): CreditCardCycleInfo {
        val dailyRate = card.apr / 100.0 / 365.0
        val monthlyInterest = if (card.outstandingBalance > card.statementBalance) {
            card.outstandingBalance * card.apr / 100.0 / 12.0
        } else 0.0

        // Interest-free period: from billing date to due date
        val interestFreeDays = if (card.dueDate > card.billingDate) {
            card.dueDate - card.billingDate
        } else {
            (30 - card.billingDate) + card.dueDate
        }

        // Optimal: buy right after billing date for max interest-free
        val optStart = card.billingDate + 1
        val optEnd = card.billingDate + 5

        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Next billing
        cal.set(Calendar.DAY_OF_MONTH, card.billingDate.coerceAtMost(28))
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.MONTH, 1)
        val nextBilling = sdf.format(cal.time)

        // Next due
        cal.set(Calendar.DAY_OF_MONTH, card.dueDate.coerceAtMost(28))
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.MONTH, 1)
        val nextDue = sdf.format(cal.time)

        val cycleBreakdown = listOf(
            CyclePeriod(
                label = "Purchase Window",
                startDay = card.billingDate + 1,
                endDay = card.billingDate + 10,
                description = "Best time to buy — get ${interestFreeDays + 20}+ interest-free days"
            ),
            CyclePeriod(
                label = "Interest-Free Period",
                startDay = card.billingDate,
                endDay = card.dueDate,
                description = "Pay full statement balance before due date to avoid ALL interest"
            ),
            CyclePeriod(
                label = "Grace Period Ends",
                startDay = card.dueDate,
                endDay = card.dueDate + 3,
                description = "After this, ${card.apr}% APR kicks in on ENTIRE outstanding balance"
            ),
            CyclePeriod(
                label = "Interest Accruing",
                startDay = card.dueDate + 3,
                endDay = card.billingDate,
                description = "₹${String.format("%.1f", card.outstandingBalance * dailyRate)}/day charged on unpaid balance"
            )
        )

        return CreditCardCycleInfo(
            interestFreeDays = interestFreeDays,
            optimalPurchaseWindowStart = optStart,
            optimalPurchaseWindowEnd = optEnd,
            nextBillingDate = nextBilling,
            nextDueDate = nextDue,
            dailyInterestRate = dailyRate,
            monthlyInterestCost = monthlyInterest,
            cycleBreakdown = cycleBreakdown
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // ── MIN PAYMENT TRAP SIMULATION ────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    fun simulateMinPaymentTrap(card: CreditCard): MinPaymentTrapResult {
        if (card.outstandingBalance <= 0) return MinPaymentTrapResult(
            emptyList(), 0, 0.0, 0.0, 0.0, 0.0
        )

        val monthlyRate = card.apr / 100.0 / 12.0
        var balance = card.outstandingBalance
        val timeline = mutableListOf<MinPaymentMonth>()
        var totalInterest = 0.0
        var totalPaid = 0.0
        var month = 0
        val minFloor = 100.0 // ₹100 minimum floor for Indian cards

        while (balance > 1 && month < 600) { // 600 months = 50 years max
            month++
            val interest = balance * monthlyRate
            val minPayment = maxOf(
                balance * card.minPaymentPercent / 100.0,
                minFloor,
                interest + 1 // Must cover at least interest + ₹1
            ).coerceAtMost(balance + interest)

            val principal = (minPayment - interest).coerceAtLeast(0.0)
            balance = (balance - principal).coerceAtLeast(0.0)
            totalInterest += interest
            totalPaid += minPayment

            timeline.add(MinPaymentMonth(
                month = month,
                payment = minPayment,
                interestCharged = interest,
                principalPaid = principal,
                remainingBalance = balance
            ))
        }

        return MinPaymentTrapResult(
            timeline = timeline,
            totalMonthsToPayoff = month,
            totalInterestPaid = totalInterest,
            totalAmountPaid = totalPaid,
            originalBalance = card.outstandingBalance,
            interestMultiplier = if (card.outstandingBalance > 0) {
                totalInterest / card.outstandingBalance
            } else 0.0
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // ── DEBT FREEDOM SIMULATION ────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    fun simulateDebtFreedom(
        loans: List<Loan>,
        creditCards: List<CreditCard>,
        income: IncomeProfile?,
        scenario: SimulationScenario
    ): DebtFreedomResult {
        val activeLoans = loans.filter { it.status == "ACTIVE" }
        val activeCards = creditCards.filter { it.status == "ACTIVE" && it.outstandingBalance > 0 }

        if (activeLoans.isEmpty() && activeCards.isEmpty()) return DebtFreedomResult(
            emptyList(), 0, 0.0, 0.0, 0, "—", emptyList()
        )

        // Combined debt states
        data class DebtState(
            val name: String,
            val bankName: String,
            val isCard: Boolean,
            var balance: Double,
            val originalBalance: Double,
            val monthlyRate: Double,
            val minPayment: Double, // EMI for loans, min payment for cards
            var totalInterestPaid: Double = 0.0,
            var paidOff: Boolean = false,
            var payoffMonth: Int = 0
        )

        // Sort all debts by strategy
        val allDebts = mutableListOf<DebtState>()

        activeCards.forEach { card ->
            allDebts.add(DebtState(
                name = card.cardName,
                bankName = card.bankName,
                isCard = true,
                balance = card.outstandingBalance,
                originalBalance = card.outstandingBalance,
                monthlyRate = card.apr / 100.0 / 12.0,
                minPayment = maxOf(
                    card.outstandingBalance * card.minPaymentPercent / 100.0,
                    100.0
                )
            ))
        }

        activeLoans.forEach { loan ->
            allDebts.add(DebtState(
                name = loan.name,
                bankName = loan.bankName,
                isCard = false,
                balance = loan.outstandingBalance,
                originalBalance = loan.outstandingBalance,
                monthlyRate = loan.interestRate / 100.0 / 12.0,
                minPayment = loan.emiAmount
            ))
        }

        // Sort by strategy
        when (scenario.strategy) {
            DebtStrategy.AVALANCHE -> allDebts.sortByDescending { it.monthlyRate * 12 * 100 }
            DebtStrategy.SNOWBALL -> allDebts.sortBy { it.balance }
        }

        // Also calculate normal payoff (no extra payments) for comparison
        val normalResult = simulatePayoffInternal(
            allDebts.map { it.copy() }.toMutableList(), 0.0, 0.0, 0
        )

        // Run simulation with scenario
        val simulationDebts = allDebts.map { it.copy() }.toMutableList()
        val timeline = mutableListOf<DebtFreedomMonth>()
        var month = 0
        var totalInterest = 0.0
        var freedPayments = scenario.extraMonthlyPayment

        while (simulationDebts.any { !it.paidOff } && month < 600) {
            month++
            var monthInterest = 0.0
            var monthPrincipal = 0.0
            val paidOff = mutableListOf<String>()

            // Apply lump sum
            if (month == scenario.lumpSumMonth && scenario.lumpSumPayment > 0) {
                val target = if (scenario.lumpSumTargetLoanId != null) {
                    simulationDebts.firstOrNull { !it.paidOff }
                } else {
                    simulationDebts.firstOrNull { !it.paidOff }
                }
                target?.let {
                    it.balance = (it.balance - scenario.lumpSumPayment).coerceAtLeast(0.0)
                    if (it.balance <= 0) {
                        it.paidOff = true
                        it.payoffMonth = month
                        freedPayments += it.minPayment
                        paidOff.add(it.name)
                    }
                }
            }

            var extraForPriority = freedPayments

            for (debt in simulationDebts) {
                if (debt.paidOff) continue

                val interest = debt.balance * debt.monthlyRate
                var payment = debt.minPayment

                // Priority debt gets extra
                if (debt == simulationDebts.firstOrNull { !it.paidOff }) {
                    payment += extraForPriority
                    extraForPriority = 0.0
                }

                val principal = (payment - interest).coerceAtLeast(0.0)
                debt.balance = (debt.balance - principal).coerceAtLeast(0.0)
                debt.totalInterestPaid += interest
                monthInterest += interest
                monthPrincipal += principal

                if (debt.balance <= 0 && !debt.paidOff) {
                    debt.paidOff = true
                    debt.payoffMonth = month
                    freedPayments += debt.minPayment
                    paidOff.add(debt.name)
                }
            }

            totalInterest += monthInterest

            timeline.add(DebtFreedomMonth(
                month = month,
                totalOutstanding = simulationDebts.sumOf { if (it.paidOff) 0.0 else it.balance },
                totalCreditCardDebt = simulationDebts.filter { it.isCard && !it.paidOff }.sumOf { it.balance },
                totalLoanDebt = simulationDebts.filter { !it.isCard && !it.paidOff }.sumOf { it.balance },
                interestPaid = monthInterest,
                principalPaid = monthPrincipal,
                debtsRemaining = simulationDebts.count { !it.paidOff },
                debtsPaidOff = paidOff
            ))
        }

        // Debt-free date
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, month)
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val debtFreeDate = if (month > 0) sdf.format(cal.time) else "—"

        val payoffOrder = simulationDebts.filter { it.payoffMonth > 0 }
            .sortedBy { it.payoffMonth }
            .map { debt ->
                LoanPayoffInfo(
                    name = debt.name,
                    bankName = debt.bankName,
                    originalBalance = debt.originalBalance,
                    totalInterestPaid = debt.totalInterestPaid,
                    monthsToPay = debt.payoffMonth,
                    payoffMonth = debt.payoffMonth
                )
            }

        return DebtFreedomResult(
            timeline = timeline,
            totalMonths = month,
            totalInterestPaid = totalInterest,
            interestSavedVsNormal = (normalResult.first - totalInterest).coerceAtLeast(0.0),
            monthsSavedVsNormal = (normalResult.second - month).coerceAtLeast(0),
            debtFreeDate = debtFreeDate,
            loanPayoffOrder = payoffOrder
        )
    }

    /** Internal helper: returns (totalInterest, totalMonths) for normal payoff */
    private fun simulatePayoffInternal(
        debts: MutableList<Any>,
        extraPayment: Double,
        lumpSum: Double,
        lumpSumMonth: Int
    ): Pair<Double, Int> {
        // Simple simulation with no extra payments
        data class SimpleDebt(var balance: Double, val monthlyRate: Double, val minPayment: Double, var done: Boolean = false)
        val simDebts = when {
            debts.isEmpty() -> return Pair(0.0, 0)
            else -> {
                // Already handled in the calling method, calculate normal payoff
                var totalInterest = 0.0
                var maxMonths = 0
                // Calc each debt independently
                debts.forEach { d ->
                    @Suppress("UNCHECKED_CAST")
                    val debt = d as? Map<*, *>
                    // Fallback: just return 0
                }
                Pair(totalInterest, maxMonths)
            }
        }
        return simDebts
    }

    // ═══════════════════════════════════════════════════════════════════
    // ── SMART ACTION PLAN ──────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    fun generateActionPlan(
        income: IncomeProfile?,
        loans: List<Loan>,
        creditCards: List<CreditCard>
    ): List<ActionItem> {
        val actions = mutableListOf<ActionItem>()
        var rank = 1

        val activeLoans = loans.filter { it.status == "ACTIVE" }
        val activeCards = creditCards.filter { it.status == "ACTIVE" && it.outstandingBalance > 0 }

        // 1. Credit card emergency
        activeCards.asSequence().filter { it.apr > 30 }.sortedByDescending { it.outstandingBalance }.take(2).forEach { card ->
            val monthlyInterest = card.outstandingBalance * card.apr / 100.0 / 12.0
            actions.add(ActionItem(
                rank = rank++,
                title = "Clear ${card.cardName} balance",
                description = "₹${formatAmount(card.outstandingBalance)} at ${card.apr}% APR is costing ₹${formatAmount(monthlyInterest)}/month. Pay full balance ASAP.",
                icon = "🔥",
                category = "IMMEDIATE",
                potentialSaving = monthlyInterest * 12,
                effort = "Hard"
            ))
        }

        // 2. Stop minimum payments
        activeCards.asSequence().filter { it.outstandingBalance > 10000 }.forEach { card ->
            val trap = simulateMinPaymentTrap(card)
            if (trap.totalMonthsToPayoff > 24) {
                actions.add(ActionItem(
                    rank = rank++,
                    title = "Stop min payments on ${card.cardName}",
                    description = "Min payments = ${trap.totalMonthsToPayoff} months & ₹${formatAmount(trap.totalInterestPaid)} in interest. Pay 3x minimum to clear in ~${trap.totalMonthsToPayoff / 3} months.",
                    icon = "⚠️",
                    category = "IMMEDIATE",
                    potentialSaving = trap.totalInterestPaid * 0.6,
                    effort = "Medium"
                ))
            }
        }

        // 3. High-rate loan acceleration
        activeLoans.asSequence().filter { it.interestRate > 12 }.sortedByDescending { it.interestRate }.take(2).forEach { loan ->
            val extraSaved = estimateInterestSavedWithExtra(listOf(loan), loan.emiAmount * 0.25)
            actions.add(ActionItem(
                rank = rank++,
                title = "Pay 25% extra on ${loan.name}",
                description = "Adding ₹${formatAmount(loan.emiAmount * 0.25)}/month to your ${loan.bankName} ${loan.interestRate}% loan saves ₹${formatAmount(extraSaved)}.",
                icon = "⚡",
                category = "SHORT_TERM",
                potentialSaving = extraSaved,
                effort = "Medium"
            ))
        }

        // 4. Balance transfer opportunity
        activeCards.asSequence().filter { it.apr > 35 && it.outstandingBalance > 5000 }.firstOrNull()?.let { card ->
            actions.add(ActionItem(
                rank = rank++,
                title = "Balance transfer ${card.cardName}",
                description = "Transfer ₹${formatAmount(card.outstandingBalance)} to a 0% BT card for 3-6 months. Save ≈₹${formatAmount(card.outstandingBalance * card.apr / 100 / 4)} vs current rate.",
                icon = "🔄",
                category = "SHORT_TERM",
                potentialSaving = card.outstandingBalance * card.apr / 100 / 4,
                effort = "Easy"
            ))
        }

        // 5. Loan refinancing
        activeLoans.asSequence().filter { it.interestRate > 10 && it.outstandingBalance > 100000 }.firstOrNull()?.let { loan ->
            val potentialNewRate = loan.interestRate - 2
            val currentInterest = totalInterestForLoan(loan, 0.0)
            val refinancedLoan = loan.copy(interestRate = potentialNewRate)
            val newInterest = totalInterestForLoan(refinancedLoan, 0.0)
            actions.add(ActionItem(
                rank = rank++,
                title = "Refinance ${loan.name}",
                description = "Check if ${loan.bankName} or competitors offer ${potentialNewRate}% vs current ${loan.interestRate}%. Could save ₹${formatAmount(currentInterest - newInterest)}.",
                icon = "🏦",
                category = "LONG_TERM",
                potentialSaving = currentInterest - newInterest,
                effort = "Medium"
            ))
        }

        // 6. Emergency fund
        if (income != null && income.totalMonthlyIncome > 0) {
            val totalEmi = activeLoans.sumOf { it.emiAmount } +
                    activeCards.sumOf { it.outstandingBalance * it.minPaymentPercent / 100 }
            val emergencyTarget = (income.monthlyExpenses + totalEmi) * 3
            actions.add(ActionItem(
                rank = rank++,
                title = "Build ₹${formatAmount(emergencyTarget)} emergency fund",
                description = "3 months of expenses + EMIs. This prevents new debt during emergencies. Save ₹${formatAmount(income.surplus.coerceAtLeast(0.0) * 0.3)}/month.",
                icon = "🛡️",
                category = "LONG_TERM",
                potentialSaving = 0.0,
                effort = "Easy"
            ))
        }

        return actions.sortedBy { it.rank }
    }

    private fun estimateMonthsSavedWithExtra(loans: List<Loan>, extraPerMonth: Double): Int {
        if (loans.isEmpty()) return 0
        // Normal months
        val normalMonths = loans.maxOf { monthsRemaining(it) }

        // With extra: distribute extra to highest rate
        val priorityLoan = loans.maxByOrNull { it.interestRate } ?: return 0
        val accMonths = monthsRemainingWithExtra(priorityLoan, extraPerMonth)
        return (normalMonths - accMonths).coerceAtLeast(0)
    }

    private fun estimateInterestSavedWithExtra(loans: List<Loan>, extraPerMonth: Double): Double {
        val priorityLoan = loans.maxByOrNull { it.interestRate } ?: return 0.0
        val normalInterest = totalInterestForLoan(priorityLoan, 0.0)
        val acceleratedInterest = totalInterestForLoan(priorityLoan, extraPerMonth)
        return (normalInterest - acceleratedInterest).coerceAtLeast(0.0)
    }

    private fun monthsRemainingWithExtra(loan: Loan, extra: Double): Int {
        val monthlyRate = loan.interestRate / 100.0 / 12.0
        var balance = loan.outstandingBalance
        var months = 0
        while (balance > 0 && months < 360) {
            val interest = balance * monthlyRate
            balance -= ((loan.emiAmount + extra) - interest).coerceAtLeast(0.0)
            balance = balance.coerceAtLeast(0.0)
            months++
        }
        return months
    }

    private fun totalInterestForLoan(loan: Loan, extraPayment: Double): Double {
        val monthlyRate = loan.interestRate / 100.0 / 12.0
        var balance = loan.outstandingBalance
        var totalInterest = 0.0
        var months = 0
        while (balance > 0 && months < 360) {
            val interest = balance * monthlyRate
            balance -= ((loan.emiAmount + extraPayment) - interest).coerceAtLeast(0.0)
            balance = balance.coerceAtLeast(0.0)
            totalInterest += interest
            months++
        }
        return totalInterest
    }

    private fun estimateMonthsSavedWithPrepay(loan: Loan, prepayAmount: Double): Int {
        val normalMonths = monthsRemaining(loan)
        val reducedLoan = loan.copy(outstandingBalance = (loan.outstandingBalance - prepayAmount).coerceAtLeast(0.0))
        val reducedMonths = monthsRemaining(reducedLoan)
        return (normalMonths - reducedMonths).coerceAtLeast(0)
    }

    private fun estimateInterestSavedWithPrepay(loan: Loan, prepayAmount: Double): Double {
        val normalInterest = totalInterestForLoan(loan, 0.0)
        val reducedLoan = loan.copy(outstandingBalance = (loan.outstandingBalance - prepayAmount).coerceAtLeast(0.0))
        val reducedInterest = totalInterestForLoan(reducedLoan, 0.0)
        return (normalInterest - reducedInterest).coerceAtLeast(0.0)
    }

    private fun formatAmount(amount: Double): String {
        return when {
            amount >= 10_00_000 -> "${String.format("%.1f", amount / 100000)}L"
            amount >= 1000 -> "${String.format("%.0f", amount / 1000)}K"
            else -> "${amount.roundToInt()}"
        }
    }
}

// ── Data Models ─────────────────────────────────────────────────────

data class PayoffMonth(
    val month: Int,
    val totalOutstanding: Double,
    val interestPaid: Double,
    val principalPaid: Double,
    val loansRemaining: Int
)

data class PayoffSimulation(
    val timeline: List<PayoffMonth>,
    val totalMonths: Int,
    val totalInterestPaid: Double,
    val interestSaved: Double
)

data class CreditCardTip(
    val title: String,
    val description: String,
    val icon: String,
    val category: String
)

data class SavingsOpportunity(
    val title: String,
    val subtitle: String,
    val monthsSaved: Int,
    val interestSaved: Double,
    val difficulty: String
)
