package com.account_balancer.services

import com.account_balancer.models.AccountEntity
import com.account_balancer.models.AccountId
import com.account_balancer.models.LedgerAccountBalance
import com.account_balancer.repositories.AccountsRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountsRepository: AccountsRepository,
) {
    fun createAccountWithBalance(accountId: UUID): AccountEntity {
        return accountsRepository.insert(AccountEntity.of(accountId, null))
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ) // REPEATABLE_READ to avoid read skew anomaly during concurrent updates
    fun getAccountEntity(accountId: AccountId): AccountEntity {
        return accountsRepository.requiredById(accountId)
    }

    fun updateAllAccountBalances(allAccountBalances: List<LedgerAccountBalance>) {
        accountsRepository.updateAllAccountBalances(allAccountBalances)
    }
}