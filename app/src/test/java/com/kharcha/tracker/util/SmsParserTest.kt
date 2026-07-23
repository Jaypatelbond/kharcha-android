package com.kharcha.tracker.util

import com.kharcha.tracker.domain.model.PaymentMode
import com.kharcha.tracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SmsParserTest {

    @Test
    fun parseValidCreditCardPaymentViaUpi() {
        val sms = "Dear Customer, Payment of Rs. 15000.00 received towards your HDFC Bank Credit Card ending 1234 via UPI Ref 1234567890."
        val transaction = SmsParser.parse("HDFCBK", sms, System.currentTimeMillis())

        assertNotNull(transaction)
        assertEquals(15000.0, transaction?.amount)
        assertEquals(TransactionType.EXPENSE, transaction?.type)
        assertEquals("Credit Card Bill", transaction?.detectedCategory?.name)
        assertEquals(PaymentMode.UPI, transaction?.detectedPaymentMode)
        assertEquals("HDFC", transaction?.bankName)
        assertEquals("1234", transaction?.accountLast4)
    }

    @Test
    fun parseValidCreditCardPaymentCreditedKeyword() {
        val sms = "Rs. 5000.25 credited to your SBI Credit Card XX5678 towards payment recieved."
        val transaction = SmsParser.parse("SBIINB", sms, System.currentTimeMillis())

        assertNotNull(transaction)
        assertEquals(5000.25, transaction?.amount)
        assertEquals(TransactionType.EXPENSE, transaction?.type)
        assertEquals("Credit Card Bill", transaction?.detectedCategory?.name)
        assertEquals("SBI", transaction?.bankName)
        assertEquals("5678", transaction?.accountLast4)
    }

    @Test
    fun parseValidCreditCardBillNeft() {
        val sms = "Your a/c no. XX9012 is debited for Rs.10000.0 towards NEFT of your Credit Card payment."
        val transaction = SmsParser.parse("ICICIB", sms, System.currentTimeMillis())

        assertNotNull(transaction)
        assertEquals(10000.0, transaction?.amount)
        assertEquals(TransactionType.EXPENSE, transaction?.type)
        assertEquals("Credit Card Bill", transaction?.detectedCategory?.name)
        assertEquals(PaymentMode.NET_BANKING, transaction?.detectedPaymentMode)
    }

}
