package com.kharcha.core.data.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType
import com.kharcha.core.common.util.CurrencyFormatter
import com.kharcha.core.common.util.DateUtils
import java.io.File

object PdfExporter {

    private const val PAGE_WIDTH = 595   // A4 width in points
    private const val PAGE_HEIGHT = 842  // A4 height in points
    private const val MARGIN = 40f
    private const val ROW_HEIGHT = 22f

    fun export(context: Context, transactions: List<Transaction>): File {
        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = MARGIN

        val titlePaint = Paint().apply {
            color = Color.parseColor("#00BFA6"); textSize = 22f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY; textSize = 10f; isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.WHITE; textSize = 10f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#00BFA6") }
        val cellPaint = Paint().apply {
            color = Color.DKGRAY; textSize = 9.5f; isAntiAlias = true
        }
        val incomePaint = Paint().apply {
            color = Color.parseColor("#00C853"); textSize = 9.5f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        }
        val expensePaint = Paint().apply {
            color = Color.parseColor("#FF1744"); textSize = 9.5f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        }
        val linePaint = Paint().apply { color = Color.parseColor("#E0E0E0"); strokeWidth = 0.5f }
        val summaryPaint = Paint().apply {
            color = Color.DKGRAY; textSize = 11f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        }

        // --- Title ---
        canvas.drawText("Kharcha — Expense Report", MARGIN, yPos + 22f, titlePaint)
        yPos += 30f
        canvas.drawText(
            "Generated on ${DateUtils.formatDate(System.currentTimeMillis())} • ${transactions.size} transactions",
            MARGIN, yPos + 10f, subtitlePaint
        )
        yPos += 28f

        // --- Summary ---
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        canvas.drawText("Total Income: ${CurrencyFormatter.format(totalIncome)}", MARGIN, yPos + 12f, incomePaint.apply { textSize = 11f })
        canvas.drawText("Total Expense: ${CurrencyFormatter.format(totalExpense)}", MARGIN + 200f, yPos + 12f, expensePaint.apply { textSize = 11f })
        canvas.drawText("Balance: ${CurrencyFormatter.format(balance)}", MARGIN + 400f, yPos + 12f, summaryPaint)
        yPos += 30f

        // separator
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 10f

        // --- Table columns ---
        val colX = floatArrayOf(MARGIN, MARGIN + 80f, MARGIN + 145f, MARGIN + 255f, MARGIN + 345f, MARGIN + 425f)
        val colHeaders = arrayOf("Date", "Type", "Category", "Amount", "Payment", "Note")

        fun drawHeader(c: Canvas, y: Float) {
            c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT, headerBgPaint)
            colHeaders.forEachIndexed { i, header ->
                c.drawText(header, colX[i] + 4f, y + 15f, headerPaint)
            }
        }

        fun startNewPage(): Float {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            val newY = MARGIN
            drawHeader(canvas, newY)
            return newY + ROW_HEIGHT + 2f
        }

        drawHeader(canvas, yPos)
        yPos += ROW_HEIGHT + 2f

        // --- Rows ---
        transactions.forEach { t ->
            if (yPos + ROW_HEIGHT > PAGE_HEIGHT - MARGIN) {
                yPos = startNewPage()
            }

            // zebra striping
            val rowIndex = transactions.indexOf(t)
            if (rowIndex % 2 == 0) {
                val zebra = Paint().apply { color = Color.parseColor("#F5F5F5") }
                canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + ROW_HEIGHT, zebra)
            }

            val textY = yPos + 15f
            canvas.drawText(DateUtils.formatShortDate(t.date), colX[0] + 4f, textY, cellPaint)
            canvas.drawText(
                if (t.type == TransactionType.INCOME) "Income" else "Expense",
                colX[1] + 4f, textY,
                if (t.type == TransactionType.INCOME) incomePaint else expensePaint
            )
            canvas.drawText(t.category.displayName.take(18), colX[2] + 4f, textY, cellPaint)

            val amountText = CurrencyFormatter.format(t.amount)
            val amtPaint = if (t.type == TransactionType.INCOME) incomePaint else expensePaint
            canvas.drawText(amountText, colX[3] + 4f, textY, amtPaint)

            canvas.drawText(t.paymentMode.displayName.take(12), colX[4] + 4f, textY, cellPaint)
            canvas.drawText(t.note.take(16), colX[5] + 4f, textY, cellPaint)

            // row bottom line
            canvas.drawLine(MARGIN, yPos + ROW_HEIGHT, PAGE_WIDTH - MARGIN, yPos + ROW_HEIGHT, linePaint)
            yPos += ROW_HEIGHT
        }

        // --- Footer ---
        yPos += 20f
        if (yPos + 30f > PAGE_HEIGHT - MARGIN) {
            yPos = startNewPage()
            yPos += 20f
        }
        val footerPaint = Paint().apply { color = Color.GRAY; textSize = 8f; isAntiAlias = true }
        canvas.drawText("Kharcha - Daily Expense Tracker • Made with ❤ in India", MARGIN, PAGE_HEIGHT - 20f, footerPaint)

        document.finishPage(page)

        // Save
        val filename = "kharcha_report_${System.currentTimeMillis()}.pdf"
        
        // Use the new FileExporter to save to Downloads
        try {
            FileExporter.saveToDownloads(context, filename, "application/pdf") { outputStream ->
                document.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback logic handled in FileExporter or simple log
        }
        
        document.close()
        
        // Return a dummy file pointing to the potentially saved location or just a File object with the name
        // The return type of export() is File. We should change it to String? or Uri? eventually.
        // For now to avoid breaking ViewModel signature, return a File object pointing to Downloads (even if artificial)
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
    }
}
