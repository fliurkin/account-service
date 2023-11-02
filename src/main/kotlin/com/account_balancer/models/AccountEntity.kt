package com.account_balancer.models

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class AccountEntity(
    val id: AccountId,
    val balance: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun of(
            name: String,
            balance: BigDecimal,
        ) =
            AccountEntity(
                id = UUID.randomUUID(),
                balance = balance,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }
}