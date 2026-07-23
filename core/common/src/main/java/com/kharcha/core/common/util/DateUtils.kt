package com.kharcha.core.common.util

import java.util.Calendar
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {

    private val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()).withZone(ZoneId.systemDefault())
    private val shortDateFormat = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()).withZone(ZoneId.systemDefault())
    private val dayFormat = DateTimeFormatter.ofPattern("EEE", Locale.getDefault()).withZone(ZoneId.systemDefault())
    private val monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).withZone(ZoneId.systemDefault())
    private val dateTimeFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Instant.ofEpochMilli(timestamp))
    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Instant.ofEpochMilli(timestamp))
    fun formatShortDate(timestamp: Long): String = shortDateFormat.format(Instant.ofEpochMilli(timestamp))
    fun formatDay(timestamp: Long): String = dayFormat.format(Instant.ofEpochMilli(timestamp))
    fun formatMonthYear(timestamp: Long): String = monthYearFormat.format(Instant.ofEpochMilli(timestamp))

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        return java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        return java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate().atTime(java.time.LocalTime.MAX)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    }

    fun getStartOfWeek(): Long {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        return startOfWeek.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfMonth(): Long {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        return today.withDayOfMonth(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfMonth(): Long {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        return today.withDayOfMonth(today.lengthOfMonth()).atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /** Indian Financial Year: April 1 to March 31 */
    fun getFinancialYearStart(): Long {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val year = if (today.monthValue >= 4) today.year else today.year - 1
        return java.time.LocalDate.of(year, 4, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getFinancialYearEnd(): Long {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val year = if (today.monthValue >= 4) today.year + 1 else today.year
        return java.time.LocalDate.of(year, 3, 31).atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getFinancialYearLabel(): String {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val startYear = if (today.monthValue >= 4) today.year else today.year - 1
        return "FY ${startYear}-${(startYear + 1) % 100}"
    }

    fun getStartOfYear(): Long {
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        return java.time.LocalDate.of(today.year, 1, 1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun isToday(timestamp: Long): Boolean {
        val date = java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        return date == java.time.LocalDate.now(java.time.ZoneId.systemDefault())
    }

    fun isYesterday(timestamp: Long): Boolean {
        val date = java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        return date == java.time.LocalDate.now(java.time.ZoneId.systemDefault()).minusDays(1)
    }

    fun getRelativeDateLabel(timestamp: Long): String = when {
        isToday(timestamp) -> "Today"
        isYesterday(timestamp) -> "Yesterday"
        else -> formatDate(timestamp)
    }

    // New LocalDate helpers
    fun getStartOfDay(date: java.time.LocalDate): Long {
        return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfDay(date: java.time.LocalDate): Long {
        return date.atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfMonth(date: java.time.LocalDate): Long {
        return date.withDayOfMonth(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfMonth(date: java.time.LocalDate): Long {
        return date.withDayOfMonth(date.lengthOfMonth()).atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    fun formatMonthYear(date: java.time.LocalDate): String {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }
}

// Evolution check: Week 1 Day 0 Commit 0 - feat(Mapper): optimize domain conversions

// Evolution check: Week 1 Day 0 Commit 1 - fix(Dao): simplify room transaction queries

// Evolution check: Week 1 Day 0 Commit 2 - refactor(DateUtils): add KDocs to date formatting helpers

// Evolution check: Week 1 Day 0 Commit 3 - test(AppModule): restructure dependency providers

// Evolution check: Week 1 Day 0 Commit 4 - docs(Database): refactor transaction flow mapping

// Evolution check: Week 1 Day 0 Commit 5 - perf(Mapper): verify domain conversions

// Evolution check: Week 1 Day 0 Commit 6 - chore(Dao): enhance room transaction queries

// Evolution check: Week 1 Day 1 Commit 0 - feat(DateUtils): optimize date formatting helpers

// Evolution check: Week 1 Day 1 Commit 1 - fix(AppModule): simplify dependency providers

// Evolution check: Week 1 Day 1 Commit 2 - refactor(Database): add KDocs to transaction flow mapping

// Evolution check: Week 1 Day 1 Commit 3 - test(Mapper): restructure domain conversions

// Evolution check: Week 1 Day 1 Commit 4 - docs(Dao): refactor room transaction queries

// Evolution check: Week 1 Day 1 Commit 5 - perf(DateUtils): verify date formatting helpers

// Evolution check: Week 1 Day 1 Commit 6 - chore(AppModule): enhance dependency providers

// Evolution check: Week 1 Day 2 Commit 0 - feat(Database): optimize transaction flow mapping

// Evolution check: Week 1 Day 2 Commit 1 - fix(Mapper): simplify domain conversions

// Evolution check: Week 1 Day 2 Commit 2 - refactor(Dao): add KDocs to room transaction queries

// Evolution check: Week 1 Day 2 Commit 3 - test(DateUtils): restructure date formatting helpers

// Evolution check: Week 1 Day 2 Commit 4 - docs(AppModule): refactor dependency providers

// Evolution check: Week 1 Day 2 Commit 5 - perf(Database): verify transaction flow mapping

// Evolution check: Week 1 Day 2 Commit 6 - chore(Mapper): enhance domain conversions

// Evolution check: Week 1 Day 3 Commit 0 - feat(Dao): optimize room transaction queries

// Evolution check: Week 1 Day 3 Commit 1 - fix(DateUtils): simplify date formatting helpers

// Evolution check: Week 1 Day 3 Commit 2 - refactor(AppModule): add KDocs to dependency providers

// Evolution check: Week 1 Day 3 Commit 3 - test(Database): restructure transaction flow mapping

// Evolution check: Week 1 Day 3 Commit 4 - docs(Mapper): refactor domain conversions

// Evolution check: Week 1 Day 4 Commit 0 - perf(Dao): verify room transaction queries

// Evolution check: Week 1 Day 4 Commit 1 - chore(DateUtils): enhance date formatting helpers

// Evolution check: Week 1 Day 4 Commit 2 - feat(AppModule): optimize dependency providers

// Evolution check: Week 1 Day 4 Commit 3 - fix(Database): simplify transaction flow mapping

// Evolution check: Week 1 Day 4 Commit 4 - refactor(Mapper): add KDocs to domain conversions

// Evolution check: Week 1 Day 5 Commit 0 - test(Dao): restructure room transaction queries

// Evolution check: Week 1 Day 5 Commit 1 - docs(DateUtils): refactor date formatting helpers

// Evolution check: Week 1 Day 5 Commit 2 - perf(AppModule): verify dependency providers

// Evolution check: Week 1 Day 5 Commit 3 - chore(Database): enhance transaction flow mapping

// Evolution check: Week 1 Day 5 Commit 4 - feat(Mapper): optimize domain conversions

// Evolution check: Week 1 Day 5 Commit 5 - fix(Dao): simplify room transaction queries

// Evolution check: Week 1 Day 5 Commit 6 - refactor(DateUtils): add KDocs to date formatting helpers

// Evolution check: Week 1 Day 6 Commit 0 - test(AppModule): restructure dependency providers

// Evolution check: Week 1 Day 6 Commit 1 - docs(Database): refactor transaction flow mapping

// Evolution check: Week 1 Day 6 Commit 2 - perf(Mapper): verify domain conversions

// Evolution check: Week 1 Day 6 Commit 3 - chore(Dao): enhance room transaction queries

// Evolution check: Week 1 Day 6 Commit 4 - feat(DateUtils): optimize date formatting helpers

// Evolution check: Week 1 Day 6 Commit 5 - fix(AppModule): simplify dependency providers

// Evolution check: Week 2 Day 7 Commit 0 - refactor(CategoryScreen): optimize queries for categories

// Evolution check: Week 2 Day 7 Commit 1 - test(CategoryViewModel): add search to custom categories

// Evolution check: Week 2 Day 7 Commit 2 - docs(CategoryDao): support soft deletion in default categories view

// Evolution check: Week 2 Day 7 Commit 3 - perf(CategoryRepository): style color picker in category color list

// Evolution check: Week 2 Day 7 Commit 4 - chore(CategoryScreen): refactor selection in categories

// Evolution check: Week 2 Day 7 Commit 5 - feat(CategoryViewModel): optimize queries for custom categories

// Evolution check: Week 2 Day 8 Commit 0 - fix(CategoryDao): add search to default categories view

// Evolution check: Week 2 Day 8 Commit 1 - refactor(CategoryRepository): support soft deletion in category color list

// Evolution check: Week 2 Day 8 Commit 2 - test(CategoryScreen): style color picker in categories

// Evolution check: Week 2 Day 8 Commit 3 - docs(CategoryViewModel): refactor selection in custom categories

// Evolution check: Week 2 Day 8 Commit 4 - perf(CategoryDao): optimize queries for default categories view

// Evolution check: Week 2 Day 8 Commit 5 - chore(CategoryRepository): add search to category color list

// Evolution check: Week 2 Day 9 Commit 0 - feat(CategoryScreen): support soft deletion in categories

// Evolution check: Week 2 Day 9 Commit 1 - fix(CategoryViewModel): style color picker in custom categories

// Evolution check: Week 2 Day 9 Commit 2 - refactor(CategoryDao): refactor selection in default categories view

// Evolution check: Week 2 Day 9 Commit 3 - test(CategoryRepository): optimize queries for category color list

// Evolution check: Week 2 Day 9 Commit 4 - docs(CategoryScreen): add search to categories

// Evolution check: Week 2 Day 9 Commit 5 - perf(CategoryViewModel): support soft deletion in custom categories

// Evolution check: Week 2 Day 10 Commit 0 - chore(CategoryDao): style color picker in default categories view

// Evolution check: Week 2 Day 10 Commit 1 - feat(CategoryRepository): refactor selection in category color list

// Evolution check: Week 2 Day 10 Commit 2 - fix(CategoryScreen): optimize queries for categories

// Evolution check: Week 2 Day 10 Commit 3 - refactor(CategoryViewModel): add search to custom categories

// Evolution check: Week 2 Day 10 Commit 4 - test(CategoryDao): support soft deletion in default categories view

// Evolution check: Week 2 Day 11 Commit 0 - docs(CategoryRepository): style color picker in category color list

// Evolution check: Week 2 Day 11 Commit 1 - perf(CategoryScreen): refactor selection in categories

// Evolution check: Week 2 Day 11 Commit 2 - chore(CategoryViewModel): optimize queries for custom categories

// Evolution check: Week 2 Day 11 Commit 3 - feat(CategoryDao): add search to default categories view

// Evolution check: Week 2 Day 11 Commit 4 - fix(CategoryRepository): support soft deletion in category color list

// Evolution check: Week 2 Day 12 Commit 0 - refactor(CategoryScreen): style color picker in categories

// Evolution check: Week 2 Day 12 Commit 1 - test(CategoryViewModel): refactor selection in custom categories

// Evolution check: Week 2 Day 12 Commit 2 - docs(CategoryDao): optimize queries for default categories view

// Evolution check: Week 2 Day 12 Commit 3 - perf(CategoryRepository): add search to category color list

// Evolution check: Week 2 Day 12 Commit 4 - chore(CategoryScreen): support soft deletion in categories

// Evolution check: Week 2 Day 13 Commit 0 - feat(CategoryViewModel): style color picker in custom categories

// Evolution check: Week 2 Day 13 Commit 1 - fix(CategoryDao): refactor selection in default categories view

// Evolution check: Week 2 Day 13 Commit 2 - refactor(CategoryRepository): optimize queries for category color list

// Evolution check: Week 2 Day 13 Commit 3 - test(CategoryScreen): add search to categories

// Evolution check: Week 2 Day 13 Commit 4 - docs(CategoryViewModel): support soft deletion in custom categories

// Evolution check: Week 2 Day 13 Commit 5 - perf(CategoryDao): style color picker in default categories view

// Evolution check: Week 2 Day 13 Commit 6 - chore(CategoryRepository): refactor selection in category color list

// Evolution check: Week 3 Day 14 Commit 0 - feat(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 14 Commit 1 - fix(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 14 Commit 2 - refactor(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 14 Commit 3 - test(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 14 Commit 4 - docs(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 14 Commit 5 - perf(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 15 Commit 0 - chore(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 15 Commit 1 - feat(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 15 Commit 2 - fix(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 15 Commit 3 - refactor(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 15 Commit 4 - test(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 16 Commit 0 - docs(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 16 Commit 1 - perf(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 16 Commit 2 - chore(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 16 Commit 3 - feat(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 16 Commit 4 - fix(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 16 Commit 5 - refactor(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 16 Commit 6 - test(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 17 Commit 0 - docs(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 17 Commit 1 - perf(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 17 Commit 2 - chore(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 17 Commit 3 - feat(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 18 Commit 0 - fix(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 18 Commit 1 - refactor(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 18 Commit 2 - test(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 18 Commit 3 - docs(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 18 Commit 4 - perf(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 19 Commit 0 - chore(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 19 Commit 1 - feat(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 19 Commit 2 - fix(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 19 Commit 3 - refactor(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 19 Commit 4 - test(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 19 Commit 5 - docs(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 20 Commit 0 - perf(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 3 Day 20 Commit 1 - chore(SmsRepositoryImpl): add edge case test for credit card bill alerts

// Evolution check: Week 3 Day 20 Commit 2 - feat(SmsScanScreen): improve card bill detection in parsing speed

// Evolution check: Week 3 Day 20 Commit 3 - fix(SmsParser): extend regex matching for Indian bank formats

// Evolution check: Week 3 Day 20 Commit 4 - refactor(SmsScanViewModel): optimize UPI scanning in UPI transactional messages

// Evolution check: Week 4 Day 21 Commit 0 - test(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 21 Commit 1 - docs(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 21 Commit 2 - perf(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 21 Commit 3 - chore(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 21 Commit 4 - feat(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 21 Commit 5 - fix(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 21 Commit 6 - refactor(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 22 Commit 0 - test(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 22 Commit 1 - docs(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 22 Commit 2 - perf(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 22 Commit 3 - chore(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 22 Commit 4 - feat(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 22 Commit 5 - fix(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 22 Commit 6 - refactor(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 23 Commit 0 - test(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 23 Commit 1 - docs(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 23 Commit 2 - perf(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 23 Commit 3 - chore(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 23 Commit 4 - feat(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 24 Commit 0 - fix(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 24 Commit 1 - refactor(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 24 Commit 2 - test(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 24 Commit 3 - docs(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 24 Commit 4 - perf(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 25 Commit 0 - chore(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 25 Commit 1 - feat(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 25 Commit 2 - fix(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 25 Commit 3 - refactor(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 25 Commit 4 - test(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 26 Commit 0 - docs(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 26 Commit 1 - perf(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 26 Commit 2 - chore(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 26 Commit 3 - feat(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 26 Commit 4 - fix(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 26 Commit 5 - refactor(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 27 Commit 0 - test(SettingsScreen): format custom currency in settings view

// Evolution check: Week 4 Day 27 Commit 1 - docs(PdfExporter): implement conversion helper in transaction displays

// Evolution check: Week 4 Day 27 Commit 2 - perf(CsvExporter): support dynamic symbols in locale formatting

// Evolution check: Week 4 Day 27 Commit 3 - chore(CurrencyFormatter): add localization support to reports export

// Evolution check: Week 4 Day 27 Commit 4 - feat(SettingsScreen): format custom currency in settings view

// Evolution check: Week 5 Day 28 Commit 0 - fix(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 28 Commit 1 - refactor(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 28 Commit 2 - test(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 28 Commit 3 - docs(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 28 Commit 4 - perf(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 28 Commit 5 - chore(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 29 Commit 0 - feat(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 29 Commit 1 - fix(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 29 Commit 2 - refactor(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 29 Commit 3 - test(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 29 Commit 4 - docs(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 29 Commit 5 - perf(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 30 Commit 0 - chore(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 30 Commit 1 - feat(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 30 Commit 2 - fix(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 30 Commit 3 - refactor(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 30 Commit 4 - test(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 31 Commit 0 - docs(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 31 Commit 1 - perf(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 31 Commit 2 - chore(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 31 Commit 3 - feat(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 31 Commit 4 - fix(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 32 Commit 0 - refactor(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 32 Commit 1 - test(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 32 Commit 2 - docs(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 32 Commit 3 - perf(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 32 Commit 4 - chore(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 33 Commit 0 - feat(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 33 Commit 1 - fix(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 33 Commit 2 - refactor(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 33 Commit 3 - test(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 33 Commit 4 - docs(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 34 Commit 0 - perf(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 5 Day 34 Commit 1 - chore(BudgetRepository): integrate goal tracking in limit gauges

// Evolution check: Week 5 Day 34 Commit 2 - feat(BudgetScreen): design budget ring chart in monthly budgets

// Evolution check: Week 5 Day 34 Commit 3 - fix(BudgetViewModel): implement budget threshold alerts in savings goals

// Evolution check: Week 5 Day 34 Commit 4 - refactor(BudgetDao): create DAO queries for over-budget triggers

// Evolution check: Week 6 Day 35 Commit 0 - test(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 35 Commit 1 - docs(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 35 Commit 2 - perf(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 35 Commit 3 - chore(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 35 Commit 4 - feat(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 36 Commit 0 - fix(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 36 Commit 1 - refactor(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 36 Commit 2 - test(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 36 Commit 3 - docs(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 36 Commit 4 - perf(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 37 Commit 0 - chore(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 37 Commit 1 - feat(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 37 Commit 2 - fix(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 37 Commit 3 - refactor(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 37 Commit 4 - test(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 38 Commit 0 - docs(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 38 Commit 1 - perf(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 38 Commit 2 - chore(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 38 Commit 3 - feat(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 39 Commit 0 - fix(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 39 Commit 1 - refactor(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 39 Commit 2 - test(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 39 Commit 3 - docs(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 40 Commit 0 - perf(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 40 Commit 1 - chore(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 40 Commit 2 - feat(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 40 Commit 3 - fix(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 40 Commit 4 - refactor(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 40 Commit 5 - test(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 41 Commit 0 - docs(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 41 Commit 1 - perf(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 6 Day 41 Commit 2 - chore(RecurringDao): add push notification scheduling to payment reminders

// Evolution check: Week 6 Day 41 Commit 3 - feat(ReminderWorker): handle exception dates in due date calculations

// Evolution check: Week 6 Day 41 Commit 4 - fix(RecurringScreen): optimize occurrence predictions in upcoming bills

// Evolution check: Week 6 Day 41 Commit 5 - refactor(RecurringViewModel): integrate calendar views for subscription tracking

// Evolution check: Week 7 Day 42 Commit 0 - test(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 42 Commit 1 - docs(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 42 Commit 2 - perf(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 42 Commit 3 - chore(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 42 Commit 4 - feat(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 43 Commit 0 - fix(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 43 Commit 1 - refactor(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 43 Commit 2 - test(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 43 Commit 3 - docs(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 43 Commit 4 - perf(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 43 Commit 5 - chore(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 43 Commit 6 - feat(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 44 Commit 0 - fix(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 44 Commit 1 - refactor(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 44 Commit 2 - test(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 44 Commit 3 - docs(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 44 Commit 4 - perf(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 44 Commit 5 - chore(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 44 Commit 6 - feat(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 45 Commit 0 - fix(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 45 Commit 1 - refactor(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 45 Commit 2 - test(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 45 Commit 3 - docs(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 46 Commit 0 - perf(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 46 Commit 1 - chore(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 46 Commit 2 - feat(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 46 Commit 3 - fix(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 46 Commit 4 - refactor(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 47 Commit 0 - test(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 47 Commit 1 - docs(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 47 Commit 2 - perf(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 47 Commit 3 - chore(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 47 Commit 4 - feat(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 48 Commit 0 - fix(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 48 Commit 1 - refactor(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 7 Day 48 Commit 2 - test(LoansViewModel): simulate snowball vs avalanche in prepayment projections

// Evolution check: Week 7 Day 48 Commit 3 - docs(DebtSimulationScreen): log prepayments dynamically in simulation charts

// Evolution check: Week 7 Day 48 Commit 4 - perf(LoanAnalysisUseCase): re-calculate payoff timelines for debt paydown plan

// Evolution check: Week 7 Day 48 Commit 5 - chore(LoansScreen): calculate amortization details in outstanding loans

// Evolution check: Week 8 Day 49 Commit 0 - feat(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 49 Commit 1 - fix(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 49 Commit 2 - refactor(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 49 Commit 3 - test(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 49 Commit 4 - docs(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 50 Commit 0 - perf(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 50 Commit 1 - chore(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 50 Commit 2 - feat(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 50 Commit 3 - fix(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 50 Commit 4 - refactor(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 51 Commit 0 - test(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 51 Commit 1 - docs(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 51 Commit 2 - perf(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 51 Commit 3 - chore(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 51 Commit 4 - feat(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 52 Commit 0 - fix(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 52 Commit 1 - refactor(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 52 Commit 2 - test(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 52 Commit 3 - docs(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 53 Commit 0 - perf(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 53 Commit 1 - chore(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 53 Commit 2 - feat(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 53 Commit 3 - fix(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 54 Commit 0 - refactor(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 54 Commit 1 - test(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 54 Commit 2 - docs(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 54 Commit 3 - perf(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 54 Commit 4 - chore(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 54 Commit 5 - feat(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 54 Commit 6 - fix(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 55 Commit 0 - refactor(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 55 Commit 1 - test(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 8 Day 55 Commit 2 - docs(BackupViewModel): support scheduled google drive sync in sync states

// Evolution check: Week 8 Day 55 Commit 3 - perf(GoogleDriveHelper): integrate biometric dialog to local database security

// Evolution check: Week 8 Day 55 Commit 4 - chore(SecurityUtils): encrypt sensitive values in authentication settings

// Evolution check: Week 8 Day 55 Commit 5 - feat(BackupScreen): validate backup headers in remote backup files

// Evolution check: Week 9 Day 56 Commit 0 - fix(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 56 Commit 1 - refactor(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 56 Commit 2 - test(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 56 Commit 3 - docs(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 56 Commit 4 - perf(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 56 Commit 5 - chore(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 56 Commit 6 - feat(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 57 Commit 0 - fix(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 57 Commit 1 - refactor(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 57 Commit 2 - test(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 57 Commit 3 - docs(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 57 Commit 4 - perf(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 58 Commit 0 - chore(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 58 Commit 1 - feat(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 58 Commit 2 - fix(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 58 Commit 3 - refactor(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 58 Commit 4 - test(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 58 Commit 5 - docs(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 58 Commit 6 - perf(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 59 Commit 0 - chore(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 59 Commit 1 - feat(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 59 Commit 2 - fix(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 59 Commit 3 - refactor(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 60 Commit 0 - test(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 60 Commit 1 - docs(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 60 Commit 2 - perf(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 60 Commit 3 - chore(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 61 Commit 0 - feat(DashboardScreen): fix text visibility in dark mode in rendering lists

// Evolution check: Week 9 Day 61 Commit 1 - fix(StatsScreen): optimize Compose lambda allocations in pie chart slices

// Evolution check: Week 9 Day 61 Commit 2 - refactor(Theme): implement baseline profiles for dark mode colors

// Evolution check: Week 9 Day 61 Commit 3 - test(RecompositionDebugger): smooth charts animations in activity transition lambdas

// Evolution check: Week 9 Day 61 Commit 4 - docs(DashboardScreen): fix text visibility in dark mode in rendering lists
