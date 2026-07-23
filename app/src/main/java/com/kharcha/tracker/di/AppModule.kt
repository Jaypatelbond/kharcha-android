package com.kharcha.tracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kharcha.tracker.data.local.AppDatabase
import com.kharcha.tracker.data.local.dao.SmsTransactionDao
import com.kharcha.tracker.data.local.dao.SplitDao
import com.kharcha.tracker.data.local.dao.TransactionDao
import com.kharcha.tracker.data.local.util.CategoryDefaults
import com.kharcha.tracker.data.local.util.TestDataSeeder
import com.kharcha.tracker.data.repository.SmsRepositoryImpl
import com.kharcha.tracker.data.repository.SplitRepositoryImpl
import com.kharcha.tracker.data.repository.TransactionRepositoryImpl
import com.kharcha.tracker.domain.repository.SmsRepository
import com.kharcha.tracker.domain.repository.SplitRepository
import com.kharcha.tracker.domain.repository.TransactionRepository
import com.kharcha.tracker.util.SmsReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kharcha_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed default categories on fresh install
                    val defaults = CategoryDefaults.getPrepopulatedCategories()
                    defaults.forEach { cat ->
                        db.execSQL(
                            "INSERT OR IGNORE INTO categories (name, type, iconName, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf(cat.name, cat.type, cat.iconName, cat.color, if (cat.isDefault) 1 else 0, cat.createdAt)
                        )
                    }
                    // Seed test data ONLY in debug builds
                    if (com.kharcha.tracker.BuildConfig.DEBUG) {
                        TestDataSeeder.seed(db)
                    }
                }
            })
            .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): com.kharcha.tracker.data.local.dao.CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideRecurringDao(database: AppDatabase): com.kharcha.tracker.data.local.dao.RecurringDao = database.recurringDao()

    @Provides
    @Singleton
    fun provideLoanDao(database: AppDatabase): com.kharcha.tracker.data.local.dao.LoanDao = database.loanDao()

    @Provides
    @Singleton
    fun provideCreditCardDao(database: AppDatabase): com.kharcha.tracker.data.local.dao.CreditCardDao = database.creditCardDao()

    @Provides
    @Singleton
    fun provideIncomeDao(database: AppDatabase): com.kharcha.tracker.data.local.dao.IncomeDao = database.incomeDao()

    @Provides
    @Singleton
    fun provideSmsTransactionDao(db: AppDatabase): SmsTransactionDao = db.smsTransactionDao()

    @Provides
    @Singleton
    fun provideSplitDao(db: AppDatabase): SplitDao = db.splitDao()

    @Provides
    @Singleton
    fun provideSplitRepository(dao: SplitDao): SplitRepository = SplitRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository =
        TransactionRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: com.kharcha.tracker.data.local.dao.CategoryDao): com.kharcha.tracker.domain.repository.CategoryRepository =
        com.kharcha.tracker.data.repository.CategoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSmsReader(@ApplicationContext context: Context): SmsReader = SmsReader(context)

    @Provides
    @Singleton
    fun provideSmsRepository(
        dao: SmsTransactionDao,
        transactionRepository: TransactionRepository,
        smsReader: SmsReader
    ): SmsRepository = SmsRepositoryImpl(dao, transactionRepository, smsReader)

    @Provides
    @Singleton
    fun provideAdFreeRepository(
        @ApplicationContext context: Context
    ): com.kharcha.tracker.domain.repository.AdFreeRepository = com.kharcha.tracker.domain.repository.AdFreeRepository(context)

    @Provides
    @Singleton
    fun provideBudgetDao(db: AppDatabase): com.kharcha.tracker.data.local.dao.BudgetDao = db.budgetDao()

    @Provides
    @Singleton
    fun provideBudgetRepository(
        dao: com.kharcha.tracker.data.local.dao.BudgetDao
    ): com.kharcha.tracker.domain.repository.BudgetRepository =
        com.kharcha.tracker.data.repository.BudgetRepositoryImpl(dao)
}
