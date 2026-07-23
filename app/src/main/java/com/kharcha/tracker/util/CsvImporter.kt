package com.kharcha.tracker.util

import com.kharcha.tracker.data.mapper.getFallbackCategory
import com.kharcha.tracker.domain.model.Category
import com.kharcha.tracker.domain.model.PaymentMode
import com.kharcha.tracker.domain.model.Transaction
import com.kharcha.tracker.domain.model.TransactionType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

object CsvImporter {

    data class ImportResult(
        val successCount: Int,
        val failureCount: Int,
        val errors: List<String>
    )

    fun parse(inputStream: InputStream, availableCategories: List<Category>): Pair<List<Transaction>, ImportResult> {
        val transactions = mutableListOf<Transaction>()
        val errors = mutableListOf<String>()
        var successCount = 0
        var failureCount = 0
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip header
            reader.readLine()

            var line: String? = reader.readLine()
            var lineNumber = 2 

            while (line != null) {
                if (line.isBlank()) {
                    line = reader.readLine()
                    continue
                }
                
                try {
                    val tokens = parseCsvLine(line)
                    
                    if (tokens.size >= 6) {
                        val dateStr = tokens[0]
                        val typeStr = tokens[1]
                        val categoryStr = tokens[2]
                        val amountStr = tokens[3]
                        val paymentModeStr = tokens[4]
                        val note = tokens[5].removeSurrounding("\"")

                        val date = dateFormat.parse(dateStr)?.time ?: throw Exception("Invalid date format")
                        val type = TransactionType.valueOf(typeStr)
                        // Find category by name or fallback
                        val category = availableCategories.find { it.name.equals(categoryStr, ignoreCase = true) } 
                            ?: getFallbackCategory(categoryStr, typeStr)
                        
                        val amount = amountStr.toDoubleOrNull() ?: throw Exception("Invalid amount")
                        
                        val paymentMode = PaymentMode.entries.find { it.displayName.equals(paymentModeStr, ignoreCase = true) } 
                            ?: PaymentMode.CASH

                        val transaction = Transaction(
                            amount = amount,
                            category = category,
                            date = date,
                            note = note,
                            type = type,
                            paymentMode = paymentMode
                        )
                        transactions.add(transaction)
                        successCount++
                    } else {
                        failureCount++
                        errors.add("Line $lineNumber: Insufficient columns")
                    }
                } catch (e: Exception) {
                    failureCount++
                    errors.add("Line $lineNumber: ${e.message}")
                }
                
                lineNumber++
                line = reader.readLine()
            }
        } catch (e: Exception) {
            errors.add("General: ${e.message}")
        }

        return Pair(transactions, ImportResult(successCount, failureCount, errors))
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(char)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
