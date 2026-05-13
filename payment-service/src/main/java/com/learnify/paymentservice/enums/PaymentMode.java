package com.learnify.paymentservice.enums;

public enum PaymentMode {
    CARD,    // Credit / Debit Card
    UPI,     // Unified Payments Interface (India)
    WALLET,  // Platform wallet / digital wallet
    NET_BANKING,
    FREE     // Used for free courses (amount = 0)
}