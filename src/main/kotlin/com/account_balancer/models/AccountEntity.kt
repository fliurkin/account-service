package com.account_balancer.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountEntity(
    val id: AccountId,
    val balance: BigDecimal,
    val currencyCode: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun of(
            accountId: AccountId,
            balance: BigDecimal? = null,
        ) =
            AccountEntity(
                id = accountId,
                balance = balance ?: BigDecimal.ZERO,
                currencyCode = "EUR",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }
}
