package com.account_balancer.models

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class MoneyBookingOrderEntity(
    val id: MoneyBookingId,
    val checkoutId: CheckoutId,
    val customerId: AccountId,
    val tenantId: AccountId,
    val status: MoneyBookingStatus,
    val amount: BigDecimal,
    val currencyCode: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val ledgerUpdatedAt: LocalDateTime? = null
) {
    companion object {
        fun of(
            checkoutId: CheckoutId,
            customerId: AccountId,
            tenantId: AccountId,
            amount: BigDecimal,
        ) =
            MoneyBookingOrderEntity(
                id = UUID.randomUUID(),
                checkoutId = checkoutId,
                customerId = customerId,
                tenantId = tenantId,
                status = MoneyBookingStatus.PENDING,
                amount = amount,
                currencyCode = "EUR",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }
}

enum class MoneyBookingStatus {
    PENDING,
    SUCCESS,
    CANCELLED,
}