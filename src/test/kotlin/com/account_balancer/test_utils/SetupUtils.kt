package com.account_balancer.test_utils

import com.account_balancer.models.AccountEntity
import com.account_balancer.repositories.AccountsRepository
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class SetupUtils(
    private val accountsRepository: AccountsRepository
) {
    fun setupAccount(initialBalance: BigDecimal? = null): AccountEntity {
        return accountsRepository.insert(AccountEntity.of(UUID.randomUUID(), initialBalance))
    }
}