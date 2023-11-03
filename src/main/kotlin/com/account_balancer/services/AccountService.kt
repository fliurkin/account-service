package com.account_balancer.services

import com.account_balancer.models.AccountEntity
import com.account_balancer.models.AccountId
import com.account_balancer.models.LedgerAccountBalance
import com.account_balancer.repositories.AccountsRepository
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountsRepository: AccountsRepository,
) {
    fun createAccountWithBalance(accountId: UUID, initialBalance: BigDecimal?): AccountEntity {
        return accountsRepository.insert(
            AccountEntity.of(accountId, initialBalance)
        )
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun getAccountEntity(accountId: AccountId): AccountEntity {
        return accountsRepository.requiredById(accountId)
    }

    fun updateAllAccountBalances(allAccountBalances: List<LedgerAccountBalance>) {
        accountsRepository.updateAllAccountBalances(allAccountBalances)
    }
}