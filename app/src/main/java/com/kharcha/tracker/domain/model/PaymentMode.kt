package com.kharcha.tracker.domain.model

enum class PaymentMode(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking"),
    WALLET("Wallet"),
    OTHER("Other");
}
