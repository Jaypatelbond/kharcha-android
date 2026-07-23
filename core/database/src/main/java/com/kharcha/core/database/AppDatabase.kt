package com.kharcha.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kharcha.core.database.dao.CreditCardDao
import com.kharcha.core.database.dao.IncomeDao
import com.kharcha.core.database.dao.SmsTransactionDao
import com.kharcha.core.database.dao.SplitDao
import com.kharcha.core.database.dao.TransactionDao
import com.kharcha.core.database.entity.CreditCardEntity
import com.kharcha.core.database.entity.IncomeProfileEntity
import com.kharcha.core.database.entity.SmsTransactionEntity
import com.kharcha.core.database.entity.SplitExpenseEntity
import com.kharcha.core.database.entity.SplitExpenseShareEntity
import com.kharcha.core.database.entity.SplitGroupEntity
import com.kharcha.core.database.entity.SplitMemberEntity
import com.kharcha.core.database.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        SmsTransactionEntity::class,
        SplitGroupEntity::class,
        SplitMemberEntity::class,
        SplitExpenseEntity::class,
        SplitExpenseShareEntity::class,
        com.kharcha.core.database.entity.CategoryEntity::class,
        com.kharcha.core.database.entity.RecurringTransactionEntity::class,
        com.kharcha.core.database.entity.LoanEntity::class,
        CreditCardEntity::class,
        IncomeProfileEntity::class,
        com.kharcha.core.database.entity.BudgetEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun smsTransactionDao(): SmsTransactionDao
    abstract fun splitDao(): SplitDao
    abstract fun categoryDao(): com.kharcha.core.database.dao.CategoryDao
    abstract fun recurringDao(): com.kharcha.core.database.dao.RecurringDao
    abstract fun loanDao(): com.kharcha.core.database.dao.LoanDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun incomeDao(): IncomeDao
    abstract fun budgetDao(): com.kharcha.core.database.dao.BudgetDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sms_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sender` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `type` TEXT NOT NULL,
                        `detectedCategory` TEXT NOT NULL,
                        `detectedPaymentMode` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `accountLast4` TEXT NOT NULL,
                        `refNumber` TEXT NOT NULL,
                        `balance` REAL,
                        `timestamp` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Split Groups
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `split_groups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)

                // Split Members
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `split_members` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `groupId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `phone` TEXT,
                        FOREIGN KEY(`groupId`) REFERENCES `split_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_split_members_groupId` ON `split_members` (`groupId`)")

                // Split Expenses
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `split_expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `groupId` INTEGER NOT NULL,
                        `description` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `paidByMemberId` INTEGER NOT NULL,
                        `splitType` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`groupId`) REFERENCES `split_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`paidByMemberId`) REFERENCES `split_members`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_split_expenses_groupId` ON `split_expenses` (`groupId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_split_expenses_paidByMemberId` ON `split_expenses` (`paidByMemberId`)")

                // Split Expense Shares
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `split_expense_shares` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `expenseId` INTEGER NOT NULL,
                        `memberId` INTEGER NOT NULL,
                        `shareAmount` REAL NOT NULL,
                        FOREIGN KEY(`expenseId`) REFERENCES `split_expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`memberId`) REFERENCES `split_members`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_split_expense_shares_expenseId` ON `split_expense_shares` (`expenseId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_split_expense_shares_memberId` ON `split_expense_shares` (`memberId`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `color` INTEGER NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name_type` ON `categories` (`name`, `type`)")

                // Pre-populate default categories
                val defaults = com.kharcha.core.database.util.CategoryDefaults.getPrepopulatedCategories()
                defaults.forEach { cat ->
                    database.execSQL(
                        "INSERT INTO categories (name, type, iconName, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf(cat.name, cat.type, cat.iconName, cat.color, if (cat.isDefault) 1 else 0, cat.createdAt)
                    )
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recurring Transactions
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amount` REAL NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER,
                        `lastProcessedDate` INTEGER,
                        `isActive` INTEGER NOT NULL
                    )
                """)

                // Loans
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `loans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `principalAmount` REAL NOT NULL,
                        `interestRate` REAL NOT NULL,
                        `tenureMonths` INTEGER NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `emiAmount` REAL NOT NULL,
                        `outstandingBalance` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `type` TEXT NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `recurring_transactions` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `recurring_transactions` ADD COLUMN `nextDueDate` INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Split Groups UUID
                database.execSQL("ALTER TABLE `split_groups` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_split_groups_uuid` ON `split_groups` (`uuid`)")

                // Split Members UUID
                database.execSQL("ALTER TABLE `split_members` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_split_members_uuid` ON `split_members` (`uuid`)")

                // Split Expenses UUID
                database.execSQL("ALTER TABLE `split_expenses` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_split_expenses_uuid` ON `split_expenses` (`uuid`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Credit Cards table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `credit_cards` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cardName` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `creditLimit` REAL NOT NULL,
                        `outstandingBalance` REAL NOT NULL,
                        `apr` REAL NOT NULL,
                        `minPaymentPercent` REAL NOT NULL,
                        `billingDate` INTEGER NOT NULL,
                        `dueDate` INTEGER NOT NULL,
                        `statementBalance` REAL NOT NULL,
                        `status` TEXT NOT NULL
                    )
                """)

                // Income Profiles table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `income_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `monthlySalary` REAL NOT NULL,
                        `otherIncome` REAL NOT NULL,
                        `monthlyExpenses` REAL NOT NULL,
                        `lastUpdated` INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isArchived to categories table
                database.execSQL("ALTER TABLE `categories` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")

                // Create budgets table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `month` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_categoryName_month` ON `budgets` (`categoryName`, `month`)")
            }
        }
    }
}
