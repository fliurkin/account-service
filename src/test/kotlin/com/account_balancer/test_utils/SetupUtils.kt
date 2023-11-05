package com.account_balancer.test_utils

import com.account_balancer.models.AccountEntity
import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.LedgerEntryEntity
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.repositories.AccountsRepository
import com.account_balancer.repositories.LedgerEntriesRepository
import com.account_balancer.repositories.MoneyBookingOrdersRepository
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class SetupUtils(
    private val accountsRepository: AccountsRepository,
    private val moneyBookingOrdersRepository: MoneyBookingOrdersRepository,
    private val ledgersEntriesRepository: LedgerEntriesRepository,
) {
    fun setupAccount(initialBalance: BigDecimal? = null): AccountEntity {
        return accountsRepository.insert(AccountEntity.of(UUID.randomUUID(), initialBalance))
    }

    fun setupMoneyBooking(
        checkoutId: CheckoutId,
        customerId: AccountId,
        tenantId: AccountId,
        amount: BigDecimal,
    ): Triple<MoneyBookingOrderEntity, LedgerEntryEntity, List<AccountEntity>> {
        val moneyBookingOrderEntity =
            moneyBookingOrdersRepository.insert(MoneyBookingOrderEntity.of(checkoutId, customerId, tenantId, amount))
        val ledgerEntryEntity = ledgersEntriesRepository.insert(
            LedgerEntryEntity.of(
                amount = amount,
                moneyBookingOrderId = moneyBookingOrderEntity.id,
                credit = customerId,
                debit = tenantId,
            )
        )
        val updadetAccounts = accountsRepository.updateAllAccountBalances(
            ledgersEntriesRepository.getAccountBalances(customerId, tenantId)
        )
        moneyBookingOrdersRepository.updateStatusAndLedgerUpdatedAt(
            moneyBookingOrderEntity.checkoutId,
            MoneyBookingStatus.SUCCESS,
            ledgerEntryEntity.createdAt
        )
        return Triple(moneyBookingOrderEntity, ledgerEntryEntity, updadetAccounts)
    }

    companion object JsonConstants {
        const val jsonUnitAnyNumber = "\${json-unit.any-number}"
        const val jsonUnitIgnoreElement = "\${json-unit.ignore-element}"
    }
}