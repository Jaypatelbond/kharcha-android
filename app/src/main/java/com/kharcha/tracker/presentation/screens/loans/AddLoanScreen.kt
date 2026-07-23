package com.kharcha.tracker.presentation.screens.loans

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kharcha.tracker.data.local.entity.LoanEntity
import com.kharcha.tracker.data.local.util.IndianBanksList
import com.kharcha.tracker.presentation.theme.TealPrimary
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

// ── Colors ──────────────────────────────────────────────────────────
private val GradientTeal = Color(0xFF0D9488)
private val GradientTealDark = Color(0xFF065F56)
private val SurfaceCard = Color(0xFF1E1E2E)
private val SurfaceCardLight = Color(0xFFF8FAFC)
private val GreenAccent = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddLoanScreen(
    onBack: () -> Unit,
    prefillBankName: String = "",
    prefillEmiAmount: String = "",
    viewModel: LoansViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf(prefillBankName) }
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    var emi by remember { mutableStateOf(prefillEmiAmount) }
    var selectedLoanType by remember { mutableStateOf("PERSONAL") }
    var showBankPicker by remember { mutableStateOf(false) }

    val isFromSms = prefillEmiAmount.isNotEmpty()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Auto-set interest rate when loan type changes
    LaunchedEffect(selectedLoanType) {
        if (interestRate.isEmpty()) {
            val typeInfo = IndianBanksList.loanTypes.find { it.type == selectedLoanType }
            typeInfo?.let { interestRate = it.defaultRate.toString() }
        }
    }

    // Auto-set loan name from type + bank
    LaunchedEffect(selectedLoanType, bankName) {
        if (name.isEmpty() || IndianBanksList.loanTypes.any { "${it.label}" == name || "${it.label} - $bankName" == name }) {
            val typeLabel = IndianBanksList.loanTypes.find { it.type == selectedLoanType }?.label ?: "Loan"
            name = if (bankName.isNotEmpty()) "$typeLabel - $bankName" else typeLabel
        }
    }

    // EMI Calculation
    LaunchedEffect(principal, interestRate, tenureMonths) {
        if (isFromSms && emi.isNotEmpty()) return@LaunchedEffect
        val p = principal.toDoubleOrNull()
        val r = interestRate.toDoubleOrNull()?.div(1200)
        val n = tenureMonths.toIntOrNull()
        if (p != null && r != null && n != null && r > 0) {
            val emiVal = (p * r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
            emi = String.format("%.0f", emiVal)
        } else if (p != null && r == 0.0 && n != null && n > 0) {
            emi = String.format("%.0f", p / n)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isFromSms) "Track SMS Loan" else "Add New Loan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Fill in the details below",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── SMS Banner ──────────────────────────────────────────
            if (isFromSms) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📱", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Auto-detected from SMS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                "Bank and EMI pre-filled. Add remaining details.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── 1. Loan Type Selector ───────────────────────────────
            Text(
                "Loan Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IndianBanksList.loanTypes.forEach { loanType ->
                    val isSelected = selectedLoanType == loanType.type
                    val bgColor by animateColorAsState(
                        if (isSelected) GradientTeal.copy(alpha = 0.15f)
                        else if (isDark) SurfaceCard else SurfaceCardLight,
                        label = "loanTypeChip"
                    )

                    Card(
                        onClick = {
                            selectedLoanType = loanType.type
                            // Set default interest rate for type
                            interestRate = loanType.defaultRate.toString()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, GradientTeal)
                        } else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(loanType.icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    loanType.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GradientTeal
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    loanType.typicalRateRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Bank Picker ──────────────────────────────────────
            Text(
                "Bank / Lender",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                onClick = { showBankPicker = true },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceCard else SurfaceCardLight
                ),
                border = if (bankName.isNotEmpty()) {
                    androidx.compose.foundation.BorderStroke(2.dp, GradientTeal)
                } else {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (bankName.isNotEmpty()) GradientTeal.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bankName.isNotEmpty()) {
                            Icon(
                                Icons.Rounded.Check,
                                null,
                                tint = GradientTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Rounded.AccountBalance,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (bankName.isNotEmpty()) {
                            Text(
                                bankName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Tap to change",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        } else {
                            Text(
                                "Search from 120+ banks & lenders",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── 3. Loan Name ────────────────────────────────────────
            StyledTextField(
                value = name,
                onValueChange = { name = it },
                label = "Loan Name",
                placeholder = "e.g., Home Loan - SBI",
                isDark = isDark
            )

            // ── 4. Principal & Rate Row ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledTextField(
                    value = principal,
                    onValueChange = { principal = it },
                    label = "Principal (₹)",
                    placeholder = "e.g., 500000",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                StyledTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = "Rate (% pa)",
                    placeholder = "e.g., 8.5",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(0.7f),
                    isDark = isDark
                )
            }

            // ── 5. Tenure ───────────────────────────────────────────
            StyledTextField(
                value = tenureMonths,
                onValueChange = { tenureMonths = it },
                label = "Tenure (Months)",
                placeholder = "e.g., 240",
                keyboardType = KeyboardType.Number,
                isDark = isDark
            )

            // ── 6. EMI Display ──────────────────────────────────────
            AnimatedVisibility(visible = emi.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        GradientTealDark, GradientTeal
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    if (isFromSms) "EMI from SMS" else "Estimated Monthly EMI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "₹${formatAmount(emi.toDoubleOrNull() ?: 0.0)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            if (tenureMonths.isNotEmpty() && principal.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    val totalPayable =
                                        (emi.toDoubleOrNull() ?: 0.0) * (tenureMonths.toIntOrNull()
                                            ?: 0)
                                    val totalInterest =
                                        totalPayable - (principal.toDoubleOrNull() ?: 0.0)
                                    if (totalInterest > 0) {
                                        Text(
                                            "Total Interest",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            "₹${formatAmount(totalInterest)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFA726)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── 7. Save Button ──────────────────────────────────────
            Button(
                onClick = {
                    val p = principal.toDoubleOrNull() ?: 0.0
                    val r = interestRate.toDoubleOrNull() ?: 0.0
                    val n = tenureMonths.toIntOrNull() ?: 0
                    val e = emi.toDoubleOrNull() ?: 0.0

                    if (name.isNotEmpty() && (p > 0 || e > 0)) {
                        val loan = LoanEntity(
                            name = name,
                            bankName = bankName,
                            principalAmount = p,
                            interestRate = r,
                            tenureMonths = n,
                            startDate = System.currentTimeMillis(),
                            emiAmount = e,
                            outstandingBalance = p,
                            status = "ACTIVE",
                            type = selectedLoanType
                        )
                        viewModel.addLoan(loan)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = name.isNotEmpty() && (principal.isNotEmpty() || emi.isNotEmpty()),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GradientTeal)
            ) {
                Text(
                    "Save Loan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── BANK PICKER BOTTOM SHEET ────────────────────────────────────
    if (showBankPicker) {
        BankPickerBottomSheet(
            currentSelection = bankName,
            onSelect = { selectedBank ->
                bankName = selectedBank
                showBankPicker = false
            },
            onDismiss = { showBankPicker = false },
            isDark = isDark
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// ── BANK PICKER BOTTOM SHEET ───────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BankPickerBottomSheet(
    currentSelection: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredBanks = remember(searchQuery, selectedCategory) {
        val results = if (searchQuery.isNotBlank()) {
            IndianBanksList.search(searchQuery)
        } else {
            IndianBanksList.allBanks
        }
        if (selectedCategory == "All") results
        else results.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            // Title
            Text(
                "Select Bank / Lender",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                "120+ Indian financial institutions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, e.g. SBI, HDFC, Bajaj...") },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Rounded.Close,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientTeal,
                    cursorColor = GradientTeal,
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category chips — horizontal scroll
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IndianBanksList.categories.forEach { (key, label) ->
                    val isSelected = selectedCategory == key
                    val chipBg by animateColorAsState(
                        if (isSelected) GradientTeal else Color.Transparent,
                        label = "catChip"
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                chipBg,
                                RoundedCornerShape(20.dp)
                            )
                            .then(
                                if (!isSelected) Modifier.background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(20.dp)
                                ) else Modifier
                            )
                            .clickable { selectedCategory = key }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bank list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredBanks) { bank ->
                    val isSelected = bank.name == currentSelection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) GradientTeal.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(bank.name) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    getCategoryColor(bank.category).copy(alpha = 0.12f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                bank.shortName.take(2).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(bank.category)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                bank.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                getCategoryLabel(bank.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = getCategoryColor(bank.category),
                                fontSize = 10.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                null,
                                tint = GradientTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Custom bank option at bottom
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                onSelect(searchQuery)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✏️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    if (searchQuery.isNotBlank()) "Use \"$searchQuery\""
                                    else "Can't find your bank?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Type the name and select this option",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ── STYLED TEXT FIELD ──────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isDark: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GradientTeal,
            cursorColor = GradientTeal,
            focusedLabelColor = GradientTeal
        )
    )
}

// ═══════════════════════════════════════════════════════════════════
// ── HELPERS ────────────────────────────────────────────────────────
// ═══════════════════════════════════════════════════════════════════

private fun getCategoryColor(category: String): Color = when (category) {
    "PSB" -> Color(0xFF1565C0)       // Blue
    "PRIVATE" -> Color(0xFF7C3AED)   // Purple
    "SFB" -> Color(0xFF0D9488)       // Teal
    "NBFC" -> Color(0xFFEF6C00)      // Orange
    "FINTECH" -> Color(0xFFE91E63)   // Pink
    "HFC" -> Color(0xFF4CAF50)       // Green
    "FOREIGN" -> Color(0xFF795548)   // Brown
    "PAYMENT" -> Color(0xFF00BCD4)   // Cyan
    "COOP" -> Color(0xFF607D8B)      // Blue Grey
    else -> Color.Gray
}

private fun getCategoryLabel(category: String): String = when (category) {
    "PSB" -> "Public Sector Bank"
    "PRIVATE" -> "Private Bank"
    "SFB" -> "Small Finance Bank"
    "NBFC" -> "NBFC"
    "FINTECH" -> "Fintech Lender"
    "HFC" -> "Housing Finance"
    "FOREIGN" -> "Foreign Bank"
    "PAYMENT" -> "Payment Bank"
    "COOP" -> "Co-operative Bank"
    else -> "Other"
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 1_00_00_000 -> "${String.format("%.1f", amount / 1_00_00_000)}Cr"
        amount >= 1_00_000 -> "${String.format("%.1f", amount / 100000)}L"
        amount >= 1000 -> String.format("%,.0f", amount)
        else -> "${amount.roundToInt()}"
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
