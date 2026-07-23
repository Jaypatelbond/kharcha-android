package com.kharcha.tracker.util

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.kharcha.tracker.domain.model.SmsTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsReader(private val context: Context) {

    suspend fun readSms(daysBack: Int = 90): List<SmsTransaction> = withContext(Dispatchers.IO) {
        val transactions = mutableListOf<SmsTransaction>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val cutoff = System.currentTimeMillis() - (daysBack.toLong() * 24 * 60 * 60 * 1000)
        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(cutoff.toString())

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex) ?: continue
                val body = it.getString(bodyIndex) ?: continue
                val date = it.getLong(dateIndex)

                // Filter: Bank sender IDs have hyphen (AD-SBIINB, BZ-HDFCBK, JD-ICICIB)
                // Also accept 6-digit shortcodes from some banks and VMs
                if (isBankSender(address)) {
                    val transaction = SmsParser.parse(address, body, date)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }
            }
        }
        transactions
    }

    /**
     * Checks if the SMS sender looks like a bank/financial institution.
     * Indian bank sender IDs follow these patterns:
     * - Alphabetic with hyphen: AD-SBIINB, BZ-HDFCBK, JD-ICICIB
     * - Prefix codes: XX-XXXXX (e.g., VM-PAYTMB, JX-AXISBK)
     * - Short codes: 6-digit numeric (some banks)
     */
    private fun isBankSender(address: String): Boolean {
        // Has hyphen and is NOT a regular phone number
        if (address.contains("-") && !address.matches(Regex("^[+]?[0-9-]+$"))) return true
        // 6-digit shortcode (some banks/wallets)
        if (address.matches(Regex("^[0-9]{6}$"))) return true
        // Alphabetic sender IDs (e.g., SBIINB, HDFCBK)
        if (address.matches(Regex("^[A-Za-z]{4,10}$"))) return true
        return false
    }
}
