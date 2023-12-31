package com.account_balancer.models

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class LedgerEntryEntity(
    val id: LedgerId,
    val amount: BigDecimal,
    val moneyBookingOrderId: MoneyBookingId,
    val credit: AccountId,
    val debit: AccountId,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun of(
            amount: BigDecimal,
            moneyBookingOrderId: MoneyBookingId,
            credit: AccountId,
            debit: AccountId
        ): LedgerEntryEntity {
            return LedgerEntryEntity(
                id = UUID.randomUUID(),
                amount = amount,
                moneyBookingOrderId = moneyBookingOrderId,
                credit = credit,
                debit = debit,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }
    }
}

data class LedgerAccountBalance(
    val accountId: AccountId,
    val balance: BigDecimal,
)