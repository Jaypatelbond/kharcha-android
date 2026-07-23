package com.kharcha.tracker.data.local.util

/**
 * Comprehensive list of Indian financial institutions for loan auto-complete.
 * Includes: Public Sector Banks, Private Banks, Small Finance Banks,
 * NBFCs, Fintech Lenders, Housing Finance Companies, and Foreign Banks.
 */
object IndianBanksList {

    data class BankInfo(
        val name: String,
        val shortName: String,
        val category: String // PSB, PRIVATE, SFB, NBFC, FINTECH, HFC, FOREIGN
    )

    val allBanks: List<BankInfo> by lazy {
        publicSectorBanks + privateBanks + smallFinanceBanks +
            nbfcs + fintechLenders + housingFinance + foreignBanks +
            paymentBanks + cooperativeBanks
    }

    /** Quick lookup — returns bank names matching the query (case-insensitive) */
    fun search(query: String): List<BankInfo> {
        if (query.isBlank()) return allBanks.take(20) // Show top 20 by default
        val q = query.lowercase().trim()
        return allBanks.filter {
            it.name.lowercase().contains(q) || it.shortName.lowercase().contains(q)
        }.take(15)
    }

    // ── PUBLIC SECTOR BANKS ─────────────────────────────────────────
    private val publicSectorBanks = listOf(
        BankInfo("State Bank of India", "SBI", "PSB"),
        BankInfo("Punjab National Bank", "PNB", "PSB"),
        BankInfo("Bank of Baroda", "BOB", "PSB"),
        BankInfo("Canara Bank", "Canara", "PSB"),
        BankInfo("Union Bank of India", "Union", "PSB"),
        BankInfo("Indian Bank", "Indian Bank", "PSB"),
        BankInfo("Bank of India", "BOI", "PSB"),
        BankInfo("Central Bank of India", "CBI", "PSB"),
        BankInfo("Indian Overseas Bank", "IOB", "PSB"),
        BankInfo("UCO Bank", "UCO", "PSB"),
        BankInfo("Bank of Maharashtra", "BOM", "PSB"),
        BankInfo("Punjab & Sind Bank", "PSB", "PSB"),
    )

    // ── PRIVATE BANKS ───────────────────────────────────────────────
    private val privateBanks = listOf(
        BankInfo("HDFC Bank", "HDFC", "PRIVATE"),
        BankInfo("ICICI Bank", "ICICI", "PRIVATE"),
        BankInfo("Axis Bank", "Axis", "PRIVATE"),
        BankInfo("Kotak Mahindra Bank", "Kotak", "PRIVATE"),
        BankInfo("IndusInd Bank", "IndusInd", "PRIVATE"),
        BankInfo("Yes Bank", "Yes", "PRIVATE"),
        BankInfo("IDFC First Bank", "IDFC First", "PRIVATE"),
        BankInfo("Federal Bank", "Federal", "PRIVATE"),
        BankInfo("Bandhan Bank", "Bandhan", "PRIVATE"),
        BankInfo("RBL Bank", "RBL", "PRIVATE"),
        BankInfo("South Indian Bank", "SIB", "PRIVATE"),
        BankInfo("City Union Bank", "CUB", "PRIVATE"),
        BankInfo("Karur Vysya Bank", "KVB", "PRIVATE"),
        BankInfo("Tamilnad Mercantile Bank", "TMB", "PRIVATE"),
        BankInfo("Karnataka Bank", "KTK", "PRIVATE"),
        BankInfo("DCB Bank", "DCB", "PRIVATE"),
        BankInfo("Dhanlaxmi Bank", "Dhanlaxmi", "PRIVATE"),
        BankInfo("Jammu & Kashmir Bank", "JKB", "PRIVATE"),
        BankInfo("CSB Bank", "CSB", "PRIVATE"),
        BankInfo("Nainital Bank", "Nainital", "PRIVATE"),
        BankInfo("IDBI Bank", "IDBI", "PRIVATE"),
    )

    // ── SMALL FINANCE BANKS ─────────────────────────────────────────
    private val smallFinanceBanks = listOf(
        BankInfo("AU Small Finance Bank", "AU SFB", "SFB"),
        BankInfo("Equitas Small Finance Bank", "Equitas", "SFB"),
        BankInfo("Ujjivan Small Finance Bank", "Ujjivan", "SFB"),
        BankInfo("Jana Small Finance Bank", "Jana", "SFB"),
        BankInfo("Suryoday Small Finance Bank", "Suryoday", "SFB"),
        BankInfo("Fincare Small Finance Bank", "Fincare", "SFB"),
        BankInfo("ESAF Small Finance Bank", "ESAF", "SFB"),
        BankInfo("North East Small Finance Bank", "NESFB", "SFB"),
        BankInfo("Capital Small Finance Bank", "Capital SFB", "SFB"),
        BankInfo("Shivalik Small Finance Bank", "Shivalik", "SFB"),
        BankInfo("Unity Small Finance Bank", "Unity SFB", "SFB"),
    )

    // ── NBFCs (Non-Banking Financial Companies) ─────────────────────
    private val nbfcs = listOf(
        BankInfo("Bajaj Finserv", "Bajaj", "NBFC"),
        BankInfo("Tata Capital", "Tata Capital", "NBFC"),
        BankInfo("Aditya Birla Finance", "ABF", "NBFC"),
        BankInfo("Mahindra Finance", "Mahindra", "NBFC"),
        BankInfo("L&T Finance", "L&T", "NBFC"),
        BankInfo("Muthoot Finance", "Muthoot", "NBFC"),
        BankInfo("Manappuram Finance", "Manappuram", "NBFC"),
        BankInfo("Shriram Finance", "Shriram", "NBFC"),
        BankInfo("Cholamandalam Finance", "Chola", "NBFC"),
        BankInfo("HDB Financial Services", "HDBFS", "NBFC"),
        BankInfo("IIFL Finance", "IIFL", "NBFC"),
        BankInfo("Poonawalla Fincorp", "Poonawalla", "NBFC"),
        BankInfo("Hero FinCorp", "Hero FC", "NBFC"),
        BankInfo("Fullerton India", "Fullerton", "NBFC"),
        BankInfo("Sundaram Finance", "Sundaram", "NBFC"),
        BankInfo("CreditAccess Grameen", "CreditAccess", "NBFC"),
        BankInfo("Five Star Business Finance", "Five Star", "NBFC"),
        BankInfo("Muthoot Microfin", "Muthoot Micro", "NBFC"),
        BankInfo("TVS Credit", "TVS Credit", "NBFC"),
        BankInfo("HDFC Credila", "Credila", "NBFC"),
    )

    // ── FINTECH LENDERS ─────────────────────────────────────────────
    private val fintechLenders = listOf(
        BankInfo("Paytm", "Paytm", "FINTECH"),
        BankInfo("PhonePe", "PhonePe", "FINTECH"),
        BankInfo("Google Pay (via partners)", "GPay", "FINTECH"),
        BankInfo("CRED", "CRED", "FINTECH"),
        BankInfo("Navi", "Navi", "FINTECH"),
        BankInfo("KreditBee", "KreditBee", "FINTECH"),
        BankInfo("MoneyTap", "MoneyTap", "FINTECH"),
        BankInfo("EarlySalary", "EarlySalary", "FINTECH"),
        BankInfo("mPokket", "mPokket", "FINTECH"),
        BankInfo("Rupeek", "Rupeek", "FINTECH"),
        BankInfo("Lendingkart", "Lendingkart", "FINTECH"),
        BankInfo("ZestMoney (now Zest by DMI)", "ZestMoney", "FINTECH"),
        BankInfo("Slice", "Slice", "FINTECH"),
        BankInfo("Fi Money", "Fi", "FINTECH"),
        BankInfo("Jupiter", "Jupiter", "FINTECH"),
        BankInfo("OneCard", "OneCard", "FINTECH"),
        BankInfo("Uni Cards", "Uni", "FINTECH"),
        BankInfo("Fibe (formerly EarlySalary)", "Fibe", "FINTECH"),
        BankInfo("CASHe", "CASHe", "FINTECH"),
        BankInfo("Home Credit India", "Home Credit", "FINTECH"),
        BankInfo("InCred Finance", "InCred", "FINTECH"),
        BankInfo("Kissht", "Kissht", "FINTECH"),
        BankInfo("True Balance", "TrueBalance", "FINTECH"),
        BankInfo("Stashfin", "Stashfin", "FINTECH"),
        BankInfo("Prefr", "Prefr", "FINTECH"),
        BankInfo("LazyPay", "LazyPay", "FINTECH"),
        BankInfo("Simpl", "Simpl", "FINTECH"),
        BankInfo("FlexSalary", "FlexSalary", "FINTECH"),
    )

    // ── HOUSING FINANCE COMPANIES ───────────────────────────────────
    private val housingFinance = listOf(
        BankInfo("HDFC Ltd (now merged with HDFC Bank)", "HDFC Ltd", "HFC"),
        BankInfo("LIC Housing Finance", "LIC HFL", "HFC"),
        BankInfo("PNB Housing Finance", "PNB HFL", "HFC"),
        BankInfo("Indiabulls Housing Finance", "Indiabulls HFL", "HFC"),
        BankInfo("IIFL Home Loans", "IIFL Home", "HFC"),
        BankInfo("Godrej Housing Finance", "Godrej HFL", "HFC"),
        BankInfo("Tata Capital Housing Finance", "Tata HFL", "HFC"),
        BankInfo("Bajaj Housing Finance", "Bajaj HFL", "HFC"),
        BankInfo("GIC Housing Finance", "GIC HFL", "HFC"),
        BankInfo("Can Fin Homes", "Can Fin", "HFC"),
        BankInfo("Aadhar Housing Finance", "Aadhar HFL", "HFC"),
        BankInfo("Aptus Value Housing Finance", "Aptus", "HFC"),
        BankInfo("Home First Finance", "HomeFirst", "HFC"),
        BankInfo("Repco Home Finance", "Repco", "HFC"),
    )

    // ── FOREIGN BANKS ───────────────────────────────────────────────
    private val foreignBanks = listOf(
        BankInfo("Citibank India", "Citi", "FOREIGN"),
        BankInfo("HSBC India", "HSBC", "FOREIGN"),
        BankInfo("Standard Chartered India", "StanChart", "FOREIGN"),
        BankInfo("Deutsche Bank India", "Deutsche", "FOREIGN"),
        BankInfo("DBS Bank India", "DBS", "FOREIGN"),
        BankInfo("Barclays India", "Barclays", "FOREIGN"),
    )

    // ── PAYMENT BANKS ───────────────────────────────────────────────
    private val paymentBanks = listOf(
        BankInfo("Airtel Payments Bank", "Airtel PB", "PAYMENT"),
        BankInfo("Paytm Payments Bank", "Paytm PB", "PAYMENT"),
        BankInfo("India Post Payments Bank", "IPPB", "PAYMENT"),
        BankInfo("Jio Payments Bank", "Jio PB", "PAYMENT"),
        BankInfo("NSDL Payments Bank", "NSDL PB", "PAYMENT"),
        BankInfo("Fino Payments Bank", "Fino PB", "PAYMENT"),
    )

    // ── CO-OPERATIVE BANKS ──────────────────────────────────────────
    private val cooperativeBanks = listOf(
        BankInfo("Saraswat Co-operative Bank", "Saraswat", "COOP"),
        BankInfo("Cosmos Co-operative Bank", "Cosmos", "COOP"),
        BankInfo("Shamrao Vithal Co-operative Bank", "SVC", "COOP"),
        BankInfo("TJSB Sahakari Bank", "TJSB", "COOP"),
        BankInfo("Apna Sahakari Bank", "Apna", "COOP"),
    )

    /** All category labels for filtering */
    val categories = listOf(
        "All" to "All",
        "PSB" to "Public Banks",
        "PRIVATE" to "Private Banks",
        "SFB" to "Small Finance",
        "NBFC" to "NBFCs",
        "FINTECH" to "Fintech",
        "HFC" to "Housing Finance",
        "FOREIGN" to "Foreign Banks",
        "PAYMENT" to "Payment Banks",
        "COOP" to "Co-operative"
    )

    /** Loan type presets with default interest rates */
    data class LoanTypeInfo(
        val type: String,
        val label: String,
        val icon: String,
        val typicalRateRange: String,
        val defaultRate: Double
    )

    val loanTypes = listOf(
        LoanTypeInfo("HOME", "Home Loan", "🏠", "8-10%", 8.5),
        LoanTypeInfo("CAR", "Car / Vehicle", "🚗", "8-12%", 9.0),
        LoanTypeInfo("PERSONAL", "Personal Loan", "💰", "10-24%", 14.0),
        LoanTypeInfo("EDUCATION", "Education", "🎓", "7-12%", 9.5),
        LoanTypeInfo("GOLD", "Gold Loan", "✨", "7-15%", 10.0),
        LoanTypeInfo("BUSINESS", "Business", "💼", "12-24%", 16.0),
        LoanTypeInfo("CREDIT_CARD", "Credit Card", "💳", "24-42%", 36.0),
        LoanTypeInfo("TWO_WHEELER", "Two Wheeler", "🏍️", "8-18%", 12.0),
        LoanTypeInfo("LAP", "Loan Against Property", "🏢", "9-14%", 10.5),
        LoanTypeInfo("OTHER", "Other", "📄", "—", 12.0),
    )
}
