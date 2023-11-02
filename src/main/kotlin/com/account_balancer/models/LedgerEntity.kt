package com.account_balancer.models

import java.math.BigDecimal

data class LedgerEntity(
    val id: LedgerId,
    val amount: BigDecimal,
    val credit: AccountId,
    val debit: AccountId,
)