package com.account_balancer.services

import com.account_balancer.models.LedgerEntryEntity
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.repositories.LedgerEntriesRepository
import java.math.BigDecimal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class LedgerService(
    private val ledgerEntriesRepository: LedgerEntriesRepository,
    private val accountService: AccountService
) {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateBalancesAndLedger(moneyBookingOrderEntity: MoneyBookingOrderEntity): LedgerEntryEntity {
        val amount = moneyBookingOrderEntity.amount
        return createLedgerEntriesAndUpdateBalances(amount, moneyBookingOrderEntity)
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateBalancesAndLedgerWithReversedEntry(moneyBookingOrderEntity: MoneyBookingOrderEntity): LedgerEntryEntity {
        val amount = moneyBookingOrderEntity.amount.negate()
        return createLedgerEntriesAndUpdateBalances(amount, moneyBookingOrderEntity)
    }

    private fun createLedgerEntriesAndUpdateBalances(
        amount: BigDecimal,
        moneyBookingOrderEntity: MoneyBookingOrderEntity
    ): LedgerEntryEntity {
        val ledgerEntryEntity = ledgerEntriesRepository.insert(
            LedgerEntryEntity.of(
                amount = amount,
                moneyBookingOrderId = moneyBookingOrderEntity.id,
                credit = moneyBookingOrderEntity.customerId,
                debit = moneyBookingOrderEntity.tenantId,
            )
        )
        val allAccountBalances = ledgerEntriesRepository.getAccountBalances(
            moneyBookingOrderEntity.customerId,
            moneyBookingOrderEntity.tenantId
        )
        accountService.updateAllAccountBalances(allAccountBalances)
        return ledgerEntryEntity
    }
}