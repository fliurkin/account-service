package com.account_balancer.services

import com.account_balancer.models.AccountEntity
import com.account_balancer.models.AccountId
import com.account_balancer.repositories.AccountsRepository
import com.account_balancer.util.toBigDecimalOrThrow
import com.account_balancer.util.toUuidOrThrow
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountsRepository: AccountsRepository
) {
    fun createAccountWithBalance(accountId: String, initialBalance: String?): AccountEntity {
        return accountsRepository.insert(
            AccountEntity.of(
                accountId.toUuidOrThrow(),
                initialBalance.toBigDecimalOrThrow()
            )
        )
    }

    fun getAccountEntity(accountId: AccountId): AccountEntity {
        return accountsRepository.requiredById(accountId)
    }
}