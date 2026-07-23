package com.kharcha.tracker.presentation.screens.simulation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.tracker.domain.model.*
import com.kharcha.tracker.domain.usecase.CreditCardTip
import com.kharcha.tracker.domain.usecase.SavingsOpportunity
import com.kharcha.tracker.presentation.theme.TealPrimary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// ── Color Palette ───────────────────────────────────────────────────
private val GradientTeal = Color(0xFF0D9488)
private val GradientTealDark = Color(0xFF065F56)
private val OrangeAccent = Color(0xFFFF7043)
private val AmberAccent = Color(0xFFFFA726)
private val GreenAccent = Color(0xFF4CAF50)
private val RedAccent = Color(0xFFEF5350)
private val PurpleAccent = Color(0xFF7C3AED)
private val BlueAccent = Color(0xFF3B82F6)
private val SurfaceCard = Color(0xFF1E1E2E)
private val SurfaceCardLight = Color(0xFFF8FAFC)

private fun formatCompact(amount: Double): String {
    return when {
        amount >= 1_00_00_000 -> "${String.format("%.1f", amount / 1_00_00_000)}Cr"
        amount >= 1_00_000 -> "${String.format("%.1f", amount / 100000)}L"
        amount >= 1000 -> "${String.format("%.0f", amount / 1000)}K"
        else -> "${amount.toInt()}"
    }
}

private fun formatCurrency(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return when {
        amount >= 10_000_000 -> "₹${String.format("%.1f", amount / 10_000_000)} Cr"
        amount >= 100_000 -> "₹${String.format("%.1f", amount / 100_000)} L"
        amount >= 1_000 -> "₹${fmt.format(amount.toInt())}"
        else -> "₹${String.format("%.0f", amount)}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtSimulationScreen(
    onBack: () -> Unit,
    viewModel: DebtSimulationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val salary by viewModel.monthlySalary.collectAsStateWithLifecycle()
    val otherInc by viewModel.otherIncome.collectAsStateWithLifecycle()
    val expenses by viewModel.monthlyExpenses.collectAsStateWithLifecycle()
    val showAddCard by viewModel.showAddCardDialog.collectAsStateWithLifecycle()
    val isDark = true // Following AnalysisScreen pattern

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Debt Simulator", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── 1. INCOME ANALYZER ──────────────────────────────────
            item {
                IncomeAnalyzerSection(
                    state = state,
                    salary = salary,
                    otherIncome = otherInc,
                    expenses = expenses,
                    onSalaryChange = viewModel::updateMonthlySalary,
                    onOtherIncomeChange = viewModel::updateOtherIncome,
                    onExpensesChange = viewModel::updateMonthlyExpenses,
                    onSave = viewModel::saveIncome,
                    isDark = isDark
                )
            }

            // ── 2. CREDIT CARD CYCLE SIMULATOR ──────────────────────
            if (state.creditCards.isNotEmpty()) {
                item {
                    CreditCardCycleSection(
                        state = state,
                        onSelectCard = viewModel::selectCard,
                        isDark = isDark
                    )
                }

                // ── 3. MIN PAYMENT TRAP ─────────────────────────────
                item {
                    MinPaymentTrapSection(state = state, isDark = isDark)
                }
            }

            // Add card button
            item {
                AddCreditCardButton(
                    onClick = viewModel::toggleAddCardDialog,
                    isDark = isDark
                )
            }

            // ── 4. DEBT FREEDOM SIMULATOR ───────────────────────────
            if (state.hasData) {
                item {
                    DebtFreedomSection(
                        state = state,
                        onStrategyChange = viewModel::setStrategy,
                        onExtraPaymentChange = viewModel::setExtraPayment,
                        onLumpSumChange = viewModel::setLumpSum,
                        isDark = isDark
                    )
                }
            }

            // ── 5. SMART ACTION PLAN ────────────────────────────────
            if (state.actionPlan.isNotEmpty()) {
                item {
                    ActionPlanSection(state = state, isDark = isDark)
                }
            }
            
            // ── 6. Savings Opportunities ────────────────────────────
            if (state.savingsOpportunities.isNotEmpty()) {
                item {
                    SectionHeader(
                        icon = Icons.Rounded.Savings,
                        title = "Savings Opportunities",
                        subtitle = "Ways to reduce your interest burden"
                    )
                }

                item {
                    SavingsOpportunitiesSection(state.savingsOpportunities, isDark)
                }
            }

            // ── 7. Credit Card Playbook ─────────────────────────────
            if (state.creditCardTips.isNotEmpty()) {
                item {
                    SectionHeader(
                        icon = Icons.Rounded.CreditCard,
                        title = "Credit Card Playbook",
                        subtitle = "Techniques to maximize savings on cards"
                    )
                }

                item {
                    CreditCardTipsSection(state.creditCardTips, isDark)
                }
            }

        }
        } // Box
    }

    // ── Add Credit Card Dialog ───────────────────────────────────────
    if (showAddCard) {
        AddCreditCardDialog(
            onDismiss = viewModel::toggleAddCardDialog,
            onAdd = viewModel::addCreditCard
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── SECTION 1: INCOME ANALYZER ─────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun IncomeAnalyzerSection(
    state: SimulationUiState,
    salary: String,
    otherIncome: String,
    expenses: String,
    onSalaryChange: (String) -> Unit,
    onOtherIncomeChange: (String) -> Unit,
    onExpensesChange: (String) -> Unit,
    onSave: () -> Unit,
    isDark: Boolean
) {
    SimulationCard(
        title = "Income Analyzer",
        icon = "💰",
        gradient = listOf(Color(0xFF065F56), Color(0xFF0D9488)),
        isDark = isDark
    ) {
        // Input fields
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SimulationTextField(
                value = salary,
                onValueChange = onSalaryChange,
                label = "Monthly Salary",
                prefix = "₹"
            )
            SimulationTextField(
                value = otherIncome,
                onValueChange = onOtherIncomeChange,
                label = "Other Income (freelance, rent, etc.)",
                prefix = "₹"
            )
            SimulationTextField(
                value = expenses,
                onValueChange = onExpensesChange,
                label = "Essential Monthly Expenses",
                prefix = "₹"
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientTeal
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Analyze My Income", fontWeight = FontWeight.SemiBold)
            }
        }

        // Results
        state.incomeAnalysis?.let { analysis ->
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))

            // DTI Gauge — centered
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DtiGauge(analysis)
            }

            Spacer(Modifier.height(16.dp))

            // Key metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricChip(
                    label = "Surplus",
                    value = formatCurrency(analysis.monthlySurplus),
                    color = if (analysis.monthlySurplus > 0) GreenAccent else RedAccent,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                MetricChip(
                    label = "Max New EMI",
                    value = formatCurrency(analysis.maxAffordableEmi),
                    color = BlueAccent,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                MetricChip(
                    label = "Risk",
                    value = analysis.riskLevel,
                    color = when (analysis.riskLevel) {
                        "LOW" -> GreenAccent
                        "MODERATE" -> AmberAccent
                        "HIGH" -> OrangeAccent
                        else -> RedAccent
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Suggestions
            if (analysis.suggestions.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "💡 Smart Suggestions",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                analysis.suggestions.forEach { suggestion ->
                    SuggestionCard(suggestion)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DtiGauge(analysis: IncomeAnalysis) {
    val animatedDti = remember { Animatable(0f) }
    LaunchedEffect(analysis.debtToIncomeRatio) {
        animatedDti.animateTo(
            analysis.debtToIncomeRatio.toFloat().coerceIn(0f, 100f),
            tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Debt-to-Income Ratio",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val sweepAngle = 240f
                val startAngle = 150f
                val progress = animatedDti.value / 100f

                // Background arc
                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 16f, cap = StrokeCap.Round)
                )

                // Progress arc with color
                val progressColor = when {
                    animatedDti.value <= 20 -> GreenAccent
                    animatedDti.value <= 35 -> AmberAccent
                    animatedDti.value <= 50 -> OrangeAccent
                    else -> RedAccent
                }

                drawArc(
                    color = progressColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    style = Stroke(width = 16f, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${animatedDti.value.roundToInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                Text(
                    "DTI",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        // Score label
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    analysis.debtToIncomeRatio <= 20 -> GreenAccent.copy(alpha = 0.15f)
                    analysis.debtToIncomeRatio <= 35 -> AmberAccent.copy(alpha = 0.15f)
                    analysis.debtToIncomeRatio <= 50 -> OrangeAccent.copy(alpha = 0.15f)
                    else -> RedAccent.copy(alpha = 0.15f)
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                when {
                    analysis.debtToIncomeRatio <= 20 -> "Excellent — Minimal debt load"
                    analysis.debtToIncomeRatio <= 35 -> "Good — Manageable debt"
                    analysis.debtToIncomeRatio <= 50 -> "High — Consider reducing debt"
                    else -> "Critical — Immediate action needed"
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: IncomeSuggestion) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(suggestion.icon, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    suggestion.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    suggestion.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                if (suggestion.savingsAmount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Save ${formatCurrency(suggestion.savingsAmount)}/year",
                        color = GreenAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── SECTION 2: CREDIT CARD CYCLE SIMULATOR ─────────────────────────────
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CreditCardCycleSection(
    state: SimulationUiState,
    onSelectCard: (Int) -> Unit,
    isDark: Boolean
) {
    SimulationCard(
        title = "Credit Card Cycle",
        icon = "💳",
        gradient = listOf(Color(0xFF4338CA), Color(0xFF7C3AED)),
        isDark = isDark
    ) {
        // Card selector
        if (state.creditCards.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.creditCards) { index, card ->
                    FilterChip(
                        selected = index == state.selectedCardIndex,
                        onClick = { onSelectCard(index) },
                        label = { Text(card.cardName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleAccent.copy(alpha = 0.3f),
                            labelColor = Color.White.copy(alpha = 0.7f),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        state.selectedCard?.let { card ->
            // Card summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(card.cardName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(card.bankName, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatCurrency(card.outstandingBalance), color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("of ${formatCurrency(card.creditLimit)} limit", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Utilization bar
            val utilization = if (card.creditLimit > 0) (card.outstandingBalance / card.creditLimit).toFloat() else 0f
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Utilization", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "${(utilization * 100).roundToInt()}%",
                        color = when {
                            utilization <= 0.3 -> GreenAccent
                            utilization <= 0.7 -> AmberAccent
                            else -> RedAccent
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { utilization.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = when {
                        utilization <= 0.3 -> GreenAccent
                        utilization <= 0.7 -> AmberAccent
                        else -> RedAccent
                    },
                    trackColor = Color.White.copy(alpha = 0.1f),
                )
            }
        }

        // Cycle info
        state.creditCardCycleInfo?.let { cycle ->
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))

            // Cycle timeline visual
            Text("📅 Billing Cycle", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CycleMetric("Interest-Free", "${cycle.interestFreeDays} days", GreenAccent)
                CycleMetric("Next Bill", cycle.nextBillingDate, BlueAccent)
                CycleMetric("Next Due", cycle.nextDueDate, OrangeAccent)
            }

            if (cycle.monthlyInterestCost > 0) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Text(
                            "You're paying ${formatCurrency(cycle.monthlyInterestCost)}/month in interest. Pay full statement before due date to avoid this!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Cycle periods
            Spacer(Modifier.height(12.dp))
            cycle.cycleBreakdown.forEach { period ->
                CyclePeriodRow(period)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CycleMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun CyclePeriodRow(period: CyclePeriod) {
    val color = when (period.label) {
        "Purchase Window" -> GreenAccent
        "Interest-Free Period" -> BlueAccent
        "Grace Period Ends" -> AmberAccent
        else -> RedAccent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .offset(y = 4.dp)
                .background(color, CircleShape)
        )
        Column(Modifier.weight(1f)) {
            Text(period.label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(period.description, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── SECTION 3: MIN PAYMENT TRAP ────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun MinPaymentTrapSection(state: SimulationUiState, isDark: Boolean) {
    val trap = state.minPaymentTrap ?: return

    SimulationCard(
        title = "Min Payment Trap",
        icon = "⚠️",
        gradient = listOf(Color(0xFFB91C1C), Color(0xFFEF4444)),
        isDark = isDark
    ) {
        // Shocking stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShockingStat(
                value = "${trap.totalMonthsToPayoff}",
                unit = "months",
                label = "To pay off",
                color = RedAccent
            )
            ShockingStat(
                value = formatCurrency(trap.totalInterestPaid),
                unit = "",
                label = "Interest paid",
                color = OrangeAccent
            )
            ShockingStat(
                value = "${String.format("%.1f", trap.interestMultiplier)}x",
                unit = "",
                label = "Of original debt",
                color = AmberAccent
            )
        }

        Spacer(Modifier.height(16.dp))

        // Comparison
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💡 The True Cost", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                ComparisonRow(
                    label = "Original balance",
                    value = formatCurrency(trap.originalBalance),
                    color = Color.White
                )
                ComparisonRow(
                    label = "Total you'll pay",
                    value = formatCurrency(trap.totalAmountPaid),
                    color = RedAccent
                )
                ComparisonRow(
                    label = "Pure interest wasted",
                    value = formatCurrency(trap.totalInterestPaid),
                    color = OrangeAccent
                )

                if (trap.totalMonthsToPayoff > 12) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "That's ${trap.totalMonthsToPayoff / 12} years ${trap.totalMonthsToPayoff % 12} months of payments!",
                        color = RedAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Balance decline chart
        if (trap.timeline.size > 1) {
            Spacer(Modifier.height(16.dp))
            Text("Balance Over Time", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            MinPaymentChart(trap)
        }
    }
}

@Composable
private fun ShockingStat(value: String, unit: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            if (unit.isNotEmpty()) {
                Text(" $unit", color = color.copy(alpha = 0.7f), fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp))
            }
        }
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
private fun ComparisonRow(label: String, value: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Text(value, color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun MinPaymentChart(trap: MinPaymentTrapResult) {
    val timeline = trap.timeline
    val maxBalance = trap.originalBalance.toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.03f))
    ) {
        val w = size.width
        val h = size.height
        val padding = 8f

        if (timeline.isEmpty() || maxBalance <= 0f) return@Canvas

        val step = (w - 2 * padding) / timeline.size.coerceAtLeast(1)

        // Balance line
        val path = Path()
        timeline.forEachIndexed { i, month ->
            val x = padding + i * step
            val y = h - padding - ((month.remainingBalance.toFloat() / maxBalance) * (h - 2 * padding))
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, RedAccent, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Interest area (filled)
        val interestPath = Path()
        var cumInterest = 0f
        val maxCum = trap.totalInterestPaid.toFloat()
        timeline.forEachIndexed { i, month ->
            cumInterest += month.interestCharged.toFloat()
            val x = padding + i * step
            val y = h - padding - ((cumInterest / maxCum.coerceAtLeast(1f)) * (h - 2 * padding) * 0.5f)
            if (i == 0) interestPath.moveTo(x, y) else interestPath.lineTo(x, y)
        }
        timeline.indices.reversed().forEach { i ->
            val x = padding + i * step
            interestPath.lineTo(x, h - padding)
        }
        interestPath.close()
        drawPath(interestPath, OrangeAccent.copy(alpha = 0.2f))
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── SECTION 4: DEBT FREEDOM SIMULATOR ──────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun DebtFreedomSection(
    state: SimulationUiState,
    onStrategyChange: (DebtStrategy) -> Unit,
    onExtraPaymentChange: (Double) -> Unit,
    onLumpSumChange: (Double) -> Unit,
    isDark: Boolean
) {
    SimulationCard(
        title = "Debt Freedom Simulator",
        icon = "🚀",
        gradient = listOf(Color(0xFF1E40AF), Color(0xFF3B82F6)),
        isDark = isDark
    ) {
        // Strategy toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StrategyChip("⚡ Avalanche", DebtStrategy.AVALANCHE, state.strategy, onStrategyChange, Modifier.weight(1f))
            StrategyChip("🎯 Snowball", DebtStrategy.SNOWBALL, state.strategy, onStrategyChange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // Extra payment slider
        Text("Extra Monthly Payment", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            formatCurrency(state.extraMonthlyPayment),
            color = GreenAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        val maxExtra = (state.income?.surplus ?: state.totalEmi).coerceAtLeast(1000.0)
        Slider(
            value = state.extraMonthlyPayment.toFloat(),
            onValueChange = { onExtraPaymentChange(it.toDouble()) },
            valueRange = 0f..maxExtra.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = GreenAccent,
                activeTrackColor = GreenAccent,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )

        // Lump sum slider
        Spacer(Modifier.height(8.dp))
        Text("One-Time Lump Sum", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            formatCurrency(state.lumpSumPayment),
            color = BlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        val maxLump = (state.income?.totalMonthlyIncome?.times(6) ?: 500000.0).coerceAtLeast(100000.0)
        Slider(
            value = state.lumpSumPayment.toFloat(),
            onValueChange = { onLumpSumChange(it.toDouble()) },
            valueRange = 0f..maxLump.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = BlueAccent,
                activeTrackColor = BlueAccent,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )

        // Results
        state.debtFreedom?.let { result ->
            if (result.totalMonths > 0) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                // Hero stats
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    HeroStat("🗓️", "Debt-Free By", result.debtFreeDate, GreenAccent)
                    HeroStat("💰", "Interest Saved", formatCurrency(result.interestSavedVsNormal), AmberAccent)
                    HeroStat("⏱️", "Months Saved", "${result.monthsSavedVsNormal}", BlueAccent)
                }

                // Payoff timeline chart
                if (result.timeline.size > 1) {
                    Spacer(Modifier.height(16.dp))
                    Text("Debt Payoff Timeline", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    DebtTimelineChart(result)
                }

                // Payoff order
                if (result.loanPayoffOrder.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("📋 Payoff Order", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    result.loanPayoffOrder.forEachIndexed { index, info ->
                        PayoffOrderRow(index + 1, info)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyChip(
    label: String,
    strategy: DebtStrategy,
    currentStrategy: DebtStrategy,
    onSelect: (DebtStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = strategy == currentStrategy
    Card(
        modifier = modifier.clickable { onSelect(strategy) },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BlueAccent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = if (selected) BorderStroke(1.dp, BlueAccent) else null
    ) {
        Text(
            label,
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeroStat(icon: String, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
private fun DebtTimelineChart(result: DebtFreedomResult) {
    val timeline = result.timeline
    val maxOutstanding = timeline.maxOf { it.totalOutstanding }.toFloat().coerceAtLeast(1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.03f))
    ) {
        val w = size.width
        val h = size.height
        val padding = 12f
        val step = (w - 2 * padding) / timeline.size.coerceAtLeast(1)

        // Total outstanding line
        val outstandingPath = Path()
        timeline.forEachIndexed { i, month ->
            val x = padding + i * step
            val y = h - padding - ((month.totalOutstanding.toFloat() / maxOutstanding) * (h - 2 * padding))
            if (i == 0) outstandingPath.moveTo(x, y) else outstandingPath.lineTo(x, y)
        }
        drawPath(outstandingPath, GreenAccent, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Loan vs card stacking
        if (timeline.any { it.totalCreditCardDebt > 0 }) {
            val cardPath = Path()
            timeline.forEachIndexed { i, month ->
                val x = padding + i * step
                val y = h - padding - ((month.totalCreditCardDebt.toFloat() / maxOutstanding) * (h - 2 * padding))
                if (i == 0) cardPath.moveTo(x, y) else cardPath.lineTo(x, y)
            }
            drawPath(cardPath, PurpleAccent.copy(alpha = 0.7f), style = Stroke(width = 2f, cap = StrokeCap.Round))
        }

        // Mark payoff events
        timeline.forEachIndexed { i, month ->
            if (month.debtsPaidOff.isNotEmpty()) {
                val x = padding + i * step
                drawCircle(AmberAccent, radius = 5f, center = Offset(x, h - padding - 10f))
            }
        }
    }
}

@Composable
private fun PayoffOrderRow(rank: Int, info: LoanPayoffInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rank badge
        Box(
            Modifier
                .size(28.dp)
                .background(
                    when (rank) { 1 -> GreenAccent; 2 -> BlueAccent; else -> PurpleAccent }.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Column(Modifier.weight(1f)) {
            Text(info.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(
                "${info.bankName} • Month ${info.payoffMonth}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(formatCurrency(info.originalBalance), color = Color.White, fontSize = 12.sp)
            Text(
                "Int: ${formatCurrency(info.totalInterestPaid)}",
                color = OrangeAccent.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── SECTION 5: SMART ACTION PLAN ───────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun ActionPlanSection(state: SimulationUiState, isDark: Boolean) {
    SimulationCard(
        title = "Smart Action Plan",
        icon = "🎯",
        gradient = listOf(Color(0xFF7C2D12), Color(0xFFF97316)),
        isDark = isDark
    ) {
        state.actionPlan.forEachIndexed { index, action ->
            ActionItemCard(action)
            if (index < state.actionPlan.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActionItemCard(action: ActionItem) {
    val categoryColor = when (action.category) {
        "IMMEDIATE" -> RedAccent
        "SHORT_TERM" -> AmberAccent
        else -> BlueAccent
    }
    val categoryLabel = when (action.category) {
        "IMMEDIATE" -> "Do Now"
        "SHORT_TERM" -> "This Month"
        else -> "Plan For"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(action.icon, fontSize = 18.sp)
                    Text(action.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 200.dp))
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        categoryLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                action.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (action.potentialSaving > 0) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "💰 Save ${formatCurrency(action.potentialSaving)}",
                        color = GreenAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        "• ${action.effort} effort",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// ── SHARED COMPONENTS ──────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun SimulationCard(
    title: String,
    icon: String,
    gradient: List<Color>,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(gradient),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SimulationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String = "",
    numericOnly: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (!numericOnly) {
                onValueChange(newValue)
            } else if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        prefix = if (prefix.isNotEmpty()) {{ Text(prefix, color = Color.White.copy(alpha = 0.5f)) }} else null,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = if (numericOnly) KeyboardType.Number else KeyboardType.Text),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            focusedBorderColor = GradientTeal,
            cursorColor = GradientTeal,
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            focusedLabelColor = GradientTeal,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White
        )
    )
}


@Composable
private fun MetricChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AddCreditCardButton(onClick: () -> Unit, isDark: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PurpleAccent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Add, "Add", tint = PurpleAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Credit Card", color = PurpleAccent, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Add Credit Card Dialog ──────────────────────────────────────────

@Composable
private fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Double, Double, Double, Int, Int, Double) -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var outstanding by remember { mutableStateOf("") }
    var apr by remember { mutableStateOf("42.0") }
    var minPayment by remember { mutableStateOf("5.0") }
    var billingDate by remember { mutableStateOf("1") }
    var dueDate by remember { mutableStateOf("20") }
    var statementBal by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Add Credit Card",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(4.dp))

                SimulationTextField(value = cardName, onValueChange = { cardName = it }, label = "Card Name", numericOnly = false)
                SimulationTextField(value = bankName, onValueChange = { bankName = it }, label = "Bank Name", numericOnly = false)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        SimulationTextField(value = creditLimit, onValueChange = { creditLimit = it }, label = "Credit Limit", prefix = "₹")
                    }
                    Box(Modifier.weight(1f)) {
                        SimulationTextField(value = outstanding, onValueChange = { outstanding = it }, label = "Outstanding", prefix = "₹")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        SimulationTextField(value = apr, onValueChange = { apr = it }, label = "APR %")
                    }
                    Box(Modifier.weight(1f)) {
                        SimulationTextField(value = minPayment, onValueChange = { minPayment = it }, label = "Min Pay %")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        SimulationTextField(value = billingDate, onValueChange = { billingDate = it }, label = "Bill Day")
                    }
                    Box(Modifier.weight(1f)) {
                        SimulationTextField(value = dueDate, onValueChange = { dueDate = it }, label = "Due Day")
                    }
                }
                SimulationTextField(value = statementBal, onValueChange = { statementBal = it }, label = "Statement Balance", prefix = "₹")

                Spacer(Modifier.height(8.dp))

                // Buttons row at the bottom — always visible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAdd(
                                cardName, bankName,
                                creditLimit.toDoubleOrNull() ?: 0.0,
                                outstanding.toDoubleOrNull() ?: 0.0,
                                apr.toDoubleOrNull() ?: 42.0,
                                minPayment.toDoubleOrNull() ?: 5.0,
                                billingDate.toIntOrNull() ?: 1,
                                dueDate.toIntOrNull() ?: 20,
                                statementBal.toDoubleOrNull() ?: (outstanding.toDoubleOrNull() ?: 0.0)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                        enabled = cardName.isNotBlank() && bankName.isNotBlank()
                    ) {
                        Text("Add Card")
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
// ── SAVINGS OPPORTUNITIES ──────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════

@Composable
fun SavingsOpportunitiesSection(opportunities: List<SavingsOpportunity>, isDark: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        opportunities.forEach { opp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceCard else SurfaceCardLight
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: savings icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                GreenAccent.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.TrendingDown,
                            null,
                            tint = GreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))

                    // Middle: text
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            opp.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            opp.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    // Right: stats
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "₹${formatCompact(opp.interestSaved)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                        Text(
                            "${opp.monthsSaved} months early",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ── CREDIT CARD PLAYBOOK ───────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════

@Composable
fun CreditCardTipsSection(tips: List<CreditCardTip>, isDark: Boolean) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tips.size) { index ->
            CreditCardTipCard(tips[index], isDark)
        }
    }
}

@Composable
fun CreditCardTipCard(tip: CreditCardTip, isDark: Boolean) {
    val categoryColor = when (tip.category) {
        "Critical" -> RedAccent
        "Savings" -> GreenAccent
        "Timing" -> BlueAccent
        "Strategy" -> PurpleAccent
        "Rewards" -> AmberAccent
        "Action" -> OrangeAccent
        else -> GradientTeal
    }

    Card(
        modifier = Modifier
            .width(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) SurfaceCard else SurfaceCardLight
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tip.icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                tip.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .background(categoryColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    tip.category,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ── HELPERS ────────────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════

@Composable
fun SectionHeader(icon: ImageVector, title: String, subtitle: String, isDark: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = GradientTeal, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}
