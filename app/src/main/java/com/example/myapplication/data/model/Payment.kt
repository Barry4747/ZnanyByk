package com.example.myapplication.data.model

data class Payment (
    val paymentId: String = "",
    val trainingsId: String = "",
    val amount: Double = 0.0,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val clientId: String = "",
    val trainerId: String = "",
    val method: PaymentMethod = PaymentMethod.CARD
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}

enum class PaymentMethod {
    CARD,
    CASH,
    BLIK
}

