package com.kharcha.core.model

enum class PaymentMode(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking"),
    BANK_TRANSFER("Bank Transfer"),
    WALLET("Wallet"),
    OTHER("Other");
}
