package com.account_balancer.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class MoneyBookingOrderEntity(
    val checkoutId: CheckoutId,
    val customerId: AccountId,
    val tenantId: AccountId,
//    val status: MoneyBookingStatus,
    val amount: BigDecimal,
    val currencyCode: String, //only EUR supported
    val amountSign: MoneyBookingAmountSign,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val ledgerUpdated: LocalDateTime? = null,
    val accountBalanceUpdated: LocalDateTime? = null,
) {
    companion object {
        fun of(
            checkoutId: CheckoutId,
            customerId: AccountId,
            tenantId: AccountId,
            amount: BigDecimal,
            currencyCode: String,
            amountSign: MoneyBookingAmountSign,
        ) =
            MoneyBookingOrderEntity(
                checkoutId = checkoutId,
                customerId = customerId,
                tenantId = tenantId,
//                status = MoneyBookingStatus.PENDING,
                amount = amount,
                currencyCode = currencyCode,
                amountSign = amountSign,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }
}

//todo handle asynchronously?
enum class MoneyBookingStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    CANCELLED,
}

enum class MoneyBookingAmountSign {
    POSITIVE,
    NEGATIVE,
}