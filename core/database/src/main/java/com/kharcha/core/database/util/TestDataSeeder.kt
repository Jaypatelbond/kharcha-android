package com.kharcha.core.database.util

import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

/**
 * Seeds realistic test data across ALL tables on fresh install.
 * Call from RoomDatabase.Callback.onCreate()
 */
object TestDataSeeder {

    fun seed(db: SupportSQLiteDatabase) {
        seedTransactions(db)
        seedLoans(db)
        seedRecurring(db)
        seedSplitGroups(db)
    }

    // ── Transactions (30 entries, last 60 days) ──────────────────────
    private fun seedTransactions(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        data class Txn(val amount: Double, val type: String, val category: String, val mode: String, val note: String, val daysAgo: Int)

        val txns = listOf(
            // ── Expenses ──
            Txn(250.0,  "EXPENSE", "Food & Dining",     "UPI",         "Zomato order",             0),
            Txn(45.0,   "EXPENSE", "Food & Dining",     "UPI",         "Tea & samosa — office",    1),
            Txn(899.0,  "EXPENSE", "Shopping",           "CREDIT_CARD", "Amazon — phone cable",     2),
            Txn(150.0,  "EXPENSE", "Transportation",     "UPI",         "Ola auto — station",       2),
            Txn(500.0,  "EXPENSE", "Entertainment",      "UPI",         "PVR movie tickets",        3),
            Txn(1200.0, "EXPENSE", "Groceries",          "UPI",         "D-Mart weekly groceries",  4),
            Txn(350.0,  "EXPENSE", "Food & Dining",      "CASH",        "Dinner at Barbeque Nation",5),
            Txn(15000.0,"EXPENSE", "Rent",               "NET_BANKING", "Feb rent — flat",          5),
            Txn(799.0,  "EXPENSE", "Bills & Utilities",  "UPI",         "JioFiber broadband",       7),
            Txn(499.0,  "EXPENSE", "Bills & Utilities",  "UPI",         "Airtel prepaid recharge",  8),
            Txn(200.0,  "EXPENSE", "Health & Fitness",   "UPI",         "Pharmacy — cold medicine", 10),
            Txn(2500.0, "EXPENSE", "Health & Fitness",   "UPI",         "Gym monthly fee",          12),
            Txn(3500.0, "EXPENSE", "Shopping",           "CREDIT_CARD", "Myntra — shoes",           14),
            Txn(80.0,   "EXPENSE", "Transportation",     "CASH",        "Auto rickshaw",            15),
            Txn(1500.0, "EXPENSE", "Education",          "UPI",         "Udemy course",             18),
            Txn(650.0,  "EXPENSE", "Food & Dining",      "UPI",         "Dominos — party",          20),
            Txn(4200.0, "EXPENSE", "Bills & Utilities",  "NET_BANKING", "Electricity bill",         22),
            Txn(180.0,  "EXPENSE", "Transportation",     "UPI",         "Uber to airport",          25),
            Txn(9999.0, "EXPENSE", "Shopping",           "CREDIT_CARD", "Flipkart — headphones",    28),
            Txn(320.0,  "EXPENSE", "Food & Dining",      "CASH",        "Street food — Chandni Chowk", 30),
            Txn(5000.0, "EXPENSE", "Travel",             "UPI",         "IRCTC train ticket",       35),
            Txn(750.0,  "EXPENSE", "Personal Care",      "UPI",         "Salon haircut & styling",  38),
            Txn(1100.0, "EXPENSE", "Groceries",          "UPI",         "BigBasket vegetables",     40),
            Txn(2000.0, "EXPENSE", "Entertainment",      "UPI",         "Spotify + Netflix annual", 45),

            // ── Income ──
            Txn(55000.0, "INCOME", "Salary",             "NET_BANKING", "Jan salary — Infosys",     30),
            Txn(55000.0, "INCOME", "Salary",             "NET_BANKING", "Feb salary — Infosys",     1),
            Txn(12000.0, "INCOME", "Freelance",          "UPI",         "Freelance flutter project", 15),
            Txn(5000.0,  "INCOME", "Investment Returns", "NET_BANKING", "Zerodha dividend",          20),
            Txn(2000.0,  "INCOME", "Other Income",       "UPI",         "Sold old laptop — OLX",     40),
            Txn(8000.0,  "INCOME", "Freelance",          "UPI",         "UI design project",         50)
        )

        txns.forEach { t ->
            val date = now - (t.daysAgo * day)
            db.execSQL(
                "INSERT INTO transactions (amount, type, category, paymentMode, note, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf(t.amount, t.type, t.category, t.mode, t.note, date, date)
            )
        }
    }

    // ── Loans (4 active, 1 closed) ───────────────────────────────────
    private fun seedLoans(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val month = 30L * 86_400_000L

        data class Ln(val name: String, val bank: String, val principal: Double, val rate: Double, val tenure: Int, val emi: Double, val outstanding: Double, val status: String, val type: String, val startMonthsAgo: Int)

        val loans = listOf(
            Ln("Home Loan",     "SBI",         3500000.0, 8.5,  240, 30309.0,  3250000.0, "ACTIVE", "HOME",      36),
            Ln("Car Loan",      "HDFC",        800000.0,  9.0,  60,  16607.0,  520000.0,  "ACTIVE", "CAR",       18),
            Ln("Education Loan","PNB",         500000.0,  7.5,  84,  7614.0,   380000.0,  "ACTIVE", "EDUCATION", 24),
            Ln("Personal Loan", "ICICI",       200000.0,  14.0, 36,  6834.0,   95000.0,   "ACTIVE", "PERSONAL",  15),
            Ln("Bike Loan",     "Bajaj Finance",120000.0, 12.0, 24,  5647.0,   0.0,       "CLOSED", "OTHER",     30)
        )

        loans.forEach { l ->
            val start = now - (l.startMonthsAgo * month)
            db.execSQL(
                "INSERT INTO loans (name, bankName, principalAmount, interestRate, tenureMonths, startDate, emiAmount, outstandingBalance, status, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(l.name, l.bank, l.principal, l.rate, l.tenure, start, l.emi, l.outstanding, l.status, l.type)
            )
        }
    }

    // ── Recurring Transactions (5 entries) ───────────────────────────
    private fun seedRecurring(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val month = 30L * 86_400_000L

        // categoryId references auto-generated IDs from CategoryDefaults
        // Food & Dining=1, Transportation=2, ... Rent=6, Bills=7, ...
        // We use approximate IDs — categories are inserted in order
        data class Rec(val amount: Double, val catId: Long, val note: String, val type: String, val freq: String, val startMonthsAgo: Int)

        val recs = listOf(
            Rec(15000.0, 6,  "Monthly rent — flat",              "EXPENSE", "MONTHLY", 6),
            Rec(799.0,   7,  "JioFiber internet bill",            "EXPENSE", "MONTHLY", 4),
            Rec(499.0,   7,  "Airtel prepaid recharge",           "EXPENSE", "MONTHLY", 5),
            Rec(55000.0, 19, "Monthly salary — Infosys",          "INCOME",  "MONTHLY", 12),
            Rec(2500.0,  5,  "Gym membership — Gold's Gym",       "EXPENSE", "MONTHLY", 3)
        )

        recs.forEach { r ->
            val start = now - (r.startMonthsAgo * month)
            val nextDue = now + month
            db.execSQL(
                "INSERT INTO recurring_transactions (amount, categoryId, note, type, frequency, startDate, endDate, lastProcessedDate, isActive, reminderEnabled, nextDueDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(r.amount, r.catId, r.note, r.type, r.freq, start, null, now, 1, 1, nextDue)
            )
        }
    }

    // ── Split Groups (2 groups with members, expenses & shares) ──────
    private fun seedSplitGroups(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        // ─ Group 1: Goa Trip ─
        val uuid1 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_groups (id, uuid, name, createdAt) VALUES (?, ?, ?, ?)",
            arrayOf(1L, uuid1, "Goa Trip 🏖️", now - (10 * day))
        )

        // Members
        val mUuids = List(4) { UUID.randomUUID().toString() }
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(1L, mUuids[0], 1L, "Jay", "9876543210"))
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(2L, mUuids[1], 1L, "Rahul", "9876543211"))
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(3L, mUuids[2], 1L, "Priya", "9876543212"))
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(4L, mUuids[3], 1L, "Ankit", "9876543213"))

        // Expenses
        val eUuid1 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_expenses (id, uuid, groupId, description, amount, paidByMemberId, splitType, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(1L, eUuid1, 1L, "Hotel — OYO 3 nights", 12000.0, 1L, "EQUAL", now - (9 * day), now - (9 * day))
        )
        // Shares for expense 1 (12000 / 4 = 3000 each)
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(1L, 1L, 3000.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(1L, 2L, 3000.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(1L, 3L, 3000.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(1L, 4L, 3000.0))

        val eUuid2 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_expenses (id, uuid, groupId, description, amount, paidByMemberId, splitType, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(2L, eUuid2, 1L, "Dinner — Fisherman's Wharf", 4800.0, 2L, "EQUAL", now - (8 * day), now - (8 * day))
        )
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(2L, 1L, 1200.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(2L, 2L, 1200.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(2L, 3L, 1200.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(2L, 4L, 1200.0))

        val eUuid3 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_expenses (id, uuid, groupId, description, amount, paidByMemberId, splitType, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(3L, eUuid3, 1L, "Scooter rental — 2 days", 2400.0, 3L, "EQUAL", now - (7 * day), now - (7 * day))
        )
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(3L, 1L, 600.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(3L, 2L, 600.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(3L, 3L, 600.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(3L, 4L, 600.0))

        // ─ Group 2: Flat Roommates ─
        val uuid2 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_groups (id, uuid, name, createdAt) VALUES (?, ?, ?, ?)",
            arrayOf(2L, uuid2, "Flat Roommates 🏠", now - (30 * day))
        )

        val mUuids2 = List(3) { UUID.randomUUID().toString() }
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(5L, mUuids2[0], 2L, "Jay", "9876543210"))
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(6L, mUuids2[1], 2L, "Vikram", "9876543214"))
        db.execSQL("INSERT INTO split_members (id, uuid, groupId, name, phone) VALUES (?, ?, ?, ?, ?)",
            arrayOf(7L, mUuids2[2], 2L, "Neha", "9876543215"))

        val eUuid4 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_expenses (id, uuid, groupId, description, amount, paidByMemberId, splitType, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(4L, eUuid4, 2L, "Electricity bill — Feb", 4200.0, 5L, "EQUAL", now - (5 * day), now - (5 * day))
        )
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(4L, 5L, 1400.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(4L, 6L, 1400.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(4L, 7L, 1400.0))

        val eUuid5 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_expenses (id, uuid, groupId, description, amount, paidByMemberId, splitType, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(5L, eUuid5, 2L, "D-Mart groceries", 3600.0, 6L, "EQUAL", now - (3 * day), now - (3 * day))
        )
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(5L, 5L, 1200.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(5L, 6L, 1200.0))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(5L, 7L, 1200.0))

        val eUuid6 = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO split_expenses (id, uuid, groupId, description, amount, paidByMemberId, splitType, date, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(6L, eUuid6, 2L, "WiFi — JioFiber Feb", 799.0, 7L, "EQUAL", now - (2 * day), now - (2 * day))
        )
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(6L, 5L, 266.33))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(6L, 6L, 266.33))
        db.execSQL("INSERT INTO split_expense_shares (expenseId, memberId, shareAmount) VALUES (?, ?, ?)", arrayOf(6L, 7L, 266.34))
    }
}
