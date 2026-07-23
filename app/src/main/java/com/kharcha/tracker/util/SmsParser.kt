package com.kharcha.tracker.util

import com.kharcha.tracker.data.mapper.getFallbackCategory
import com.kharcha.tracker.domain.model.Category
import com.kharcha.tracker.domain.model.PaymentMode
import com.kharcha.tracker.domain.model.SmsTransaction
import com.kharcha.tracker.domain.model.TransactionType
import java.util.regex.Pattern

/**
 * Sharp SMS parser for Indian bank transaction messages.
 * Supports SBI, HDFC, ICICI, Axis, Kotak, PNB, BOB, IDFC, YES, IndusInd,
 * Federal, Canara, Union, RBL, CITI, HSBC, AU, Bandhan, IDBI, IOB, Paytm,
 * and all major UPI/wallet services.
 */
object SmsParser {

    // ── Amount Patterns ──────────────────────────────────────────────────
    // Matches: Rs.100, Rs 1,00,000.50, INR 500, ₹2500, Rs1500.00
    private val amountRegex = Pattern.compile(
        """(?i)(?:Rs\.?\s*|INR\.?\s*|₹\s*)((?:\d{1,3}(?:,\d{2,3})+|\d+)(?:\.\d{1,2})?)"""
    )

    // ── Debit/Credit Keywords ────────────────────────────────────────────
    private val debitRegex = Pattern.compile(
        """(?i)\b(debited|deducted|spent|paid|withdrawn|transferred|purchase|sent|payment|debit|auto[\s-]?debit|txn|charged|dr)\b"""
    )
    private val creditRegex = Pattern.compile(
        """(?i)\b(credited|received|deposited|added|refund|cashback|reversed|credit|cr)\b"""
    )

    // ── Account Number ───────────────────────────────────────────────────
    // Matches: A/c XX1234, Acct ending 5678, a/c no. XX9012, card ending 3456
    private val accountRegex = Pattern.compile(
        """(?i)(?:A/?c|Acct|Account|Card|ending|ending\s*(?:with|in))\s*(?:No\.?\s*)?[XxNn*]*(\d{3,4})"""
    )

    // ── Balance ──────────────────────────────────────────────────────────
    private val balanceRegex = Pattern.compile(
        """(?i)(?:Avl?\s*Bal|Available\s*Bal(?:ance)?|Bal(?:ance)?|Avail)\s*(?:is|:)?\s*(?:Rs\.?\s*|INR\s*|₹\s*)(\d{1,3}(?:,\d{2,3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)"""
    )

    // ── UPI Detection ────────────────────────────────────────────────────
    private val upiRegex = Pattern.compile(
        """(?i)\b(UPI|Google\s*Pay|GPay|PhonePe|Paytm|BHIM|UPI\s*Ref|UPI[-\s]?txn|UPI[-\s]?cr|UPI[-\s]?dr)\b"""
    )
    private val upiRefRegex = Pattern.compile(
        """(?i)(?:UPI\s*(?:Ref|Ref\.?\s*(?:No|ID)?|txn\s*(?:id|no)?))\s*[:\s]?\s*(\d{6,12})"""
    )

    // ── VPA / Merchant Extraction ────────────────────────────────────────
    // Matches: to xyz@ybl, to VPA abc@paytm, at MERCHANT NAME
    private val vpaRegex = Pattern.compile(
        """(?i)(?:to\s+(?:VPA\s+)?|from\s+(?:VPA\s+)?)([a-zA-Z0-9._-]+@[a-zA-Z]+)"""
    )
    private val merchantRegex = Pattern.compile(
        """(?i)(?:at\s+|to\s+|towards\s+|paid\s+to\s+|trf\s+to\s+|Info:\s*)((?:[A-Z][A-Za-z0-9]+[\s&]*){1,4})"""
    )

    // ── Bank Name Detection ──────────────────────────────────────────────
    private val bankRegex = Pattern.compile(
        """(?i)\b(SBI|HDFC|ICICI|AXIS|KOTAK|PNB|BOB|IDFC|YES\s*BANK|YES|CANARA|UNION|INDUSIND|CITI|HSBC|RBL|SC|Federal|AU|Bandhan|IDBI|IOB|UCO|J&K|JKB|BARODA|SYNDICATE|VIJAYA|DENA|ALLAHABAD|ORIENTAL|ANDHRA|CENTRAL|INDIAN|IOB)\b"""
    )

    // ── Loan/EMI Detection ───────────────────────────────────────────────
    private val loanEmiRegex = Pattern.compile(
        """(?i)\b(EMI|loan\s*(?:a/?c|payment|repayment|debit)?|auto[\s-]?debit|ECS|SI[\s-]?debit|standing\s*instruction|NACH|mandate)\b"""
    )

    // ── NEFT/IMPS/RTGS ──────────────────────────────────────────────────
    private val neftImpsRegex = Pattern.compile(
        """(?i)\b(NEFT|IMPS|RTGS|IFSC|fund\s*transfer|wire\s*transfer)\b"""
    )

    // ── ATM Detection ────────────────────────────────────────────────────
    private val atmRegex = Pattern.compile(
        """(?i)\b(ATM|cash\s*withdrawal|self[-\s]?withdrawal|ATM[-\s]?WDL)\b"""
    )

    // ── Credit Card Detection ────────────────────────────────────────────
    private val creditCardRegex = Pattern.compile(
        """(?i)\b(credit\s*card|CC\s*(?:ending|payment)|card\s*(?:txn|transaction)|PIN\s*purchase)\b"""
    )

    fun parse(sender: String, body: String, timestamp: Long): SmsTransaction? {
        // 1. Check if it's a transactional SMS
        if (!isTransactional(body)) return null

        // 2. Extract Amount
        val amount = extractAmount(body) ?: return null

        // 3. Skip very small or very large amounts (likely OTP or promo)
        if (amount < 1.0 || amount > 50_000_000.0) return null

        // 4. Determine Type (Debit vs Credit)
        val type = determineType(body) ?: return null

        // 5. Extract Account Number (Last 4)
        val account = extractAccount(body)

        // 6. Extract Bank Name
        val bank = extractBankName(sender, body)

        // 7. Extract Balance
        val balance = extractBalance(body)

        // 8. Extract UPI Ref Number
        val upiRef = extractUpiRef(body)

        // 9. Extract Merchant Name
        val merchant = extractMerchant(body)

        // 10. Detect Category & Payment Mode
        val (category, paymentMode) = detectCategoryAndMode(body, type, merchant)

        // 11. Detect if this is a Loan/EMI transaction
        val isLoanEmi = loanEmiRegex.matcher(body).find()

        // 12. Build note with merchant info
        val note = buildNote(merchant, upiRef, bank, account)

        return SmsTransaction(
            sender = sender,
            body = body,
            amount = amount,
            type = type,
            detectedCategory = category,
            detectedPaymentMode = paymentMode,
            bankName = bank,
            accountLast4 = account,
            refNumber = upiRef,
            balance = balance,
            isLoanEmi = isLoanEmi,
            timestamp = timestamp
        )
    }

    private fun isTransactional(body: String): Boolean {
        if (!amountRegex.matcher(body).find()) return false
        val isDebit = debitRegex.matcher(body).find()
        val isCredit = creditRegex.matcher(body).find()
        if (!(isDebit || isCredit)) return false

        // Also check for common transactional keywords
        val lowerBody = body.lowercase()
        return lowerBody.contains("a/c") || lowerBody.contains("acct") ||
                lowerBody.contains("card") || lowerBody.contains("upi") ||
                lowerBody.contains("atm") || lowerBody.contains("neft") ||
                lowerBody.contains("imps") || lowerBody.contains("txn") ||
                lowerBody.contains("bal")
    }

    private fun extractAmount(body: String): Double? {
        val matcher = amountRegex.matcher(body)
        while (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "") ?: continue
            val amount = amountStr.toDoubleOrNull()
            if (amount != null) return amount
        }
        return null
    }

    private fun determineType(body: String): TransactionType? {
        val lowerBody = body.lowercase()

        // Check for refund/cashback first (it's credit even if "paid" appears)
        if (lowerBody.contains("refund") || lowerBody.contains("cashback") || lowerBody.contains("reversed")) {
            return TransactionType.INCOME
        }

        // Explicit check for Credit Card Bill Payment (it's an expense even if "received" or "credited" appears)
        // e.g. "Payment of Rs X received towards your credit card" or "credited to your credit card"
        val isCcPayment = (lowerBody.contains("credit card") || lowerBody.contains("cc payment")) &&
                (lowerBody.contains("received") || lowerBody.contains("credited") || lowerBody.contains("payment"))
        if (isCcPayment && !lowerBody.contains("refund")) {
            return TransactionType.EXPENSE
        }

        // Credit keywords
        if (creditRegex.matcher(body).find()) return TransactionType.INCOME

        // Debit keywords
        if (debitRegex.matcher(body).find()) return TransactionType.EXPENSE

        return null
    }

    private fun extractAccount(body: String): String {
        val matcher = accountRegex.matcher(body)
        return if (matcher.find()) matcher.group(1) ?: "" else ""
    }

    private fun extractBankName(sender: String, body: String): String {
        // Try to find known bank names in body first
        val matcher = bankRegex.matcher(body)
        if (matcher.find()) return bankDisplayName(matcher.group(1) ?: "")

        // Try to infer from sender ID (e.g., AD-SBIINB, BZ-HDFCBK)
        val senderUpper = sender.uppercase()
        return when {
            senderUpper.contains("SBI") -> "SBI"
            senderUpper.contains("HDFC") -> "HDFC"
            senderUpper.contains("ICICI") -> "ICICI"
            senderUpper.contains("AXIS") -> "Axis"
            senderUpper.contains("KOTAK") -> "Kotak"
            senderUpper.contains("PNB") -> "PNB"
            senderUpper.contains("BOB") || senderUpper.contains("BARODA") -> "Bank of Baroda"
            senderUpper.contains("IDFC") -> "IDFC First"
            senderUpper.contains("YESB") || senderUpper.contains("YES") -> "Yes Bank"
            senderUpper.contains("PAYTM") || senderUpper.contains("PYTM") -> "Paytm Bank"
            senderUpper.contains("INDUS") -> "IndusInd"
            senderUpper.contains("FEDER") -> "Federal Bank"
            senderUpper.contains("CANARA") -> "Canara Bank"
            senderUpper.contains("UNION") -> "Union Bank"
            senderUpper.contains("RBL") -> "RBL Bank"
            senderUpper.contains("AU") -> "AU Bank"
            senderUpper.contains("BANDHAN") -> "Bandhan Bank"
            senderUpper.contains("IDBI") -> "IDBI Bank"
            senderUpper.contains("CITI") -> "Citibank"
            senderUpper.contains("HSBC") -> "HSBC"
            senderUpper.contains("IOB") -> "IOB"
            else -> "Bank"
        }
    }

    private fun bankDisplayName(raw: String): String = when (raw.uppercase().trim()) {
        "SBI" -> "SBI"
        "HDFC" -> "HDFC"
        "ICICI" -> "ICICI"
        "AXIS" -> "Axis"
        "KOTAK" -> "Kotak"
        "PNB" -> "PNB"
        "BOB", "BARODA" -> "Bank of Baroda"
        "IDFC" -> "IDFC First"
        "YES", "YES BANK" -> "Yes Bank"
        "INDUSIND" -> "IndusInd"
        "FEDERAL" -> "Federal Bank"
        "CANARA" -> "Canara Bank"
        "UNION" -> "Union Bank"
        "RBL" -> "RBL Bank"
        "AU" -> "AU Bank"
        "BANDHAN" -> "Bandhan Bank"
        "IDBI" -> "IDBI Bank"
        "CITI" -> "Citibank"
        "HSBC" -> "HSBC"
        "IOB" -> "IOB"
        "SC" -> "Standard Chartered"
        else -> raw
    }

    private fun extractBalance(body: String): Double? {
        val matcher = balanceRegex.matcher(body)
        var lastMatch: String? = null
        while (matcher.find()) {
            lastMatch = matcher.group(1)
        }
        return lastMatch?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractUpiRef(body: String): String {
        val matcher = upiRefRegex.matcher(body)
        return if (matcher.find()) matcher.group(1) ?: "" else ""
    }

    private fun extractMerchant(body: String): String {
        // Try VPA first (e.g., merchant@ybl)
        val vpaMatcher = vpaRegex.matcher(body)
        if (vpaMatcher.find()) {
            val vpa = vpaMatcher.group(1) ?: ""
            // Extract readable name from VPA (e.g., "zomato@paytm" -> "Zomato")
            val name = vpa.substringBefore("@").replace(".", " ").replace("-", " ").replace("_", " ")
            if (name.length > 2) {
                return name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            }
        }

        // Try merchant keyword extraction
        val merchantMatcher = merchantRegex.matcher(body)
        if (merchantMatcher.find()) {
            val merchant = merchantMatcher.group(1)?.trim() ?: ""
            // Filter out common false positives
            val lower = merchant.lowercase()
            if (lower != "your" && lower != "dear" && lower != "the" && merchant.length > 2) {
                return merchant
            }
        }

        return ""
    }

    private fun buildNote(merchant: String, upiRef: String, bank: String, account: String): String {
        val parts = mutableListOf<String>()
        if (merchant.isNotBlank()) parts.add(merchant)
        if (upiRef.isNotBlank()) parts.add("Ref: $upiRef")
        if (account.isNotBlank()) parts.add("A/c: XX$account")
        return parts.joinToString(" | ").ifBlank { "$bank transaction" }
    }

    private fun detectCategoryAndMode(body: String, type: TransactionType, merchant: String): Pair<Category, PaymentMode> {
        val lowerBody = body.lowercase()
        val lowerMerchant = merchant.lowercase()

        // ── Payment Mode Detection ──────────────────────────────────────
        val mode = when {
            upiRegex.matcher(body).find() || lowerBody.contains("vpa") || lowerBody.contains("@") -> PaymentMode.UPI
            neftImpsRegex.matcher(body).find() -> PaymentMode.NET_BANKING
            lowerBody.contains("netbanking") || lowerBody.contains("net banking") -> PaymentMode.NET_BANKING
            loanEmiRegex.matcher(body).find() -> PaymentMode.NET_BANKING
            atmRegex.matcher(body).find() -> PaymentMode.CASH
            lowerBody.contains("debit card") || lowerBody.contains("pos") || lowerBody.contains("pin purchase") -> PaymentMode.DEBIT_CARD
            creditCardRegex.matcher(body).find() -> PaymentMode.CREDIT_CARD
            lowerBody.contains("wallet") || lowerBody.contains("paytm") -> PaymentMode.WALLET
            else -> PaymentMode.OTHER
        }

        // ── Category Detection ──────────────────────────────────────────
        val category = if (type == TransactionType.INCOME) {
            when {
                lowerBody.contains("salary") || lowerBody.contains("wages") -> getFallbackCategory("Salary", "INCOME")
                lowerBody.contains("interest") || lowerBody.contains("int.") -> getFallbackCategory("Interest", "INCOME")
                lowerBody.contains("refund") || lowerBody.contains("reversal") -> getFallbackCategory("Other Income", "INCOME")
                lowerBody.contains("cashback") -> getFallbackCategory("Other Income", "INCOME")
                lowerBody.contains("dividend") || lowerBody.contains("invest") -> getFallbackCategory("Investment Returns", "INCOME")
                lowerBody.contains("freelanc") || lowerBody.contains("consult") -> getFallbackCategory("Freelance", "INCOME")
                lowerBody.contains("gift") -> getFallbackCategory("Gift Received", "INCOME")
                else -> getFallbackCategory("Other Income", "INCOME")
            }
        } else {
            // Expense categories – check merchant first, then keywords
            when {
                // Food & Dining
                matchesAny(lowerBody, lowerMerchant, "zomato", "swiggy", "restaurant", "dominos", "mcdonalds", "kfc", "pizza hut", "burger king", "dunkin", "subway", "starbucks", "cafe coffee day", "haldiram", "barbeque", "biryani", "food") ->
                    getFallbackCategory("Food & Dining", "EXPENSE")

                // Groceries
                matchesAny(lowerBody, lowerMerchant, "blinkit", "zepto", "bigbasket", "jiomart", "grofers", "dmart", "more supermarket", "ratnadeep", "reliance fresh", "nature basket", "grocery", "supermarket", "kirana", "provisions") ->
                    getFallbackCategory("Groceries", "EXPENSE")

                // Transport
                matchesAny(lowerBody, lowerMerchant, "uber", "ola", "rapido", "metro", "irctc", "makemytrip", "goibibo", "redbus", "flybus", "bluesmart", "meru", "yulu") ->
                    getFallbackCategory("Transport", "EXPENSE")

                // Fuel
                matchesAny(lowerBody, lowerMerchant, "fuel", "petrol", "diesel", "iocl", "bpcl", "hpcl", "indian oil", "hp petrol", "bharat petroleum", "shell") ->
                    getFallbackCategory("Fuel/LPG", "EXPENSE")

                // Bills & Recharge
                matchesAny(lowerBody, lowerMerchant, "recharge", "airtel", "jio", "vi ", "vodafone", "idea", "bsnl", "bill", "postpaid", "prepaid", "dth", "tata play", "d2h", "broadband") ->
                    getFallbackCategory("Recharge & Bills", "EXPENSE")

                // Electricity
                matchesAny(lowerBody, lowerMerchant, "electricity", "electric", "bescom", "msedcl", "tpddl", "uppcl", "wbsedcl", "tneb", "kseb", "dgvcl", "torrent power", "adani electricity", "power bill") ->
                    getFallbackCategory("Electricity", "EXPENSE")

                // Shopping
                matchesAny(lowerBody, lowerMerchant, "amazon", "flipkart", "myntra", "meesho", "ajio", "nykaa", "tatacliq", "snapdeal", "shopsy", "croma", "reliance digital", "vijay sales", "decathlon", "ikea", "shopping") ->
                    getFallbackCategory("Shopping", "EXPENSE")

                // Credit Card Bill
                (lowerBody.contains("credit card") || lowerBody.contains("cc payment")) &&
                (lowerBody.contains("received") || lowerBody.contains("credited") || lowerBody.contains("payment")) ->
                    getFallbackCategory("Credit Card Bill", "EXPENSE")

                // EMI/Loan
                loanEmiRegex.matcher(body).find() || matchesAny(lowerBody, lowerMerchant, "emi", "loan", "nach", "ecs", "mandate", "standing instruction") ->
                    getFallbackCategory("EMI/Loan", "EXPENSE")

                // Subscriptions
                matchesAny(lowerBody, lowerMerchant, "netflix", "prime", "hotstar", "spotify", "youtube", "disney", "zee5", "sonyliv", "jiocinema", "subscription", "apple music", "audible", "coursera") ->
                    getFallbackCategory("Subscriptions", "EXPENSE")

                // Health
                matchesAny(lowerBody, lowerMerchant, "pharmeasy", "netmeds", "1mg", "apollo", "medplus", "practo", "hospital", "clinic", "medical", "pharmacy", "diagnostic", "pathlab") ->
                    getFallbackCategory("Health & Medical", "EXPENSE")

                // Education
                matchesAny(lowerBody, lowerMerchant, "school", "college", "university", "tuition", "coaching", "byju", "unacademy", "vedantu", "upgrad", "simplilearn", "education") ->
                    getFallbackCategory("Education", "EXPENSE")

                // Entertainment
                matchesAny(lowerBody, lowerMerchant, "bookmyshow", "pvr", "inox", "cinema", "movie", "amusement", "gaming", "dream11", "mpl") ->
                    getFallbackCategory("Entertainment", "EXPENSE")

                // Rent
                matchesAny(lowerBody, lowerMerchant, "rent", "landlord", "pg ", "hostel") ->
                    getFallbackCategory("Rent", "EXPENSE")

                // Gifts
                matchesAny(lowerBody, lowerMerchant, "gift", "donation", "charity", "ngo") ->
                    getFallbackCategory("Gifts & Donations", "EXPENSE")

                // Chai/Snacks
                matchesAny(lowerBody, lowerMerchant, "chai", "tea", "snack", "chaayos", "chai point") ->
                    getFallbackCategory("Chai/Snacks", "EXPENSE")

                // ATM Withdrawal
                atmRegex.matcher(body).find() ->
                    getFallbackCategory("Others", "EXPENSE")

                else -> getFallbackCategory("Others", "EXPENSE")
            }
        }

        return category to mode
    }

    /**
     * Checks if any of the keywords appear in either the SMS body or the merchant name.
     */
    private fun matchesAny(lowerBody: String, lowerMerchant: String, vararg keywords: String): Boolean {
        return keywords.any { keyword ->
            lowerBody.contains(keyword) || lowerMerchant.contains(keyword)
        }
    }
}
