package com.account_balancer.services

import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.MoneyBookingId
import com.account_balancer.models.MoneyBookingOrderEntity
import java.math.BigDecimal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class MoneyTransactionsService(
    private val moneyBookingOrdersService: MoneyBookingOrdersService,
    private val ledgerService: LedgerService
) {
    @Transactional(isolation = Isolation.REPEATABLE_READ) // REPEATABLE_READ to avoid race condition
    fun bookMoney(
        checkoutId: CheckoutId,
        customerId: AccountId,
        tenantId: AccountId,
        amount: BigDecimal
    ): MoneyBookingOrderEntity {
        val moneyBookingOrderEntity =
            moneyBookingOrdersService.createMoneyBookingOrder(checkoutId, customerId, tenantId, amount)

        // TODO implement it asynchronously so that latency is not affected by using queue with redriving mechanism
        val ledgerEntry = ledgerService.updateBalancesAndLedger(moneyBookingOrderEntity)
        return moneyBookingOrdersService.completeMoneyBookingOrder(moneyBookingOrderEntity, ledgerEntry)
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ) // REPEATABLE_READ to avoid race condition
    fun cancelMoneyBooking(moneyBookingId: MoneyBookingId): MoneyBookingOrderEntity {
        val moneyBookingOrder = moneyBookingOrdersService.getMoneyBookingOrder(moneyBookingId)

        // TODO implement it asynchronously so that latency is not affected by using queue with redriving mechanism
        val ledgerEntry = ledgerService.updateBalancesAndLedgerWithReversedEntry(moneyBookingOrder)
        return moneyBookingOrdersService.cancelMoneyBookingOrder(moneyBookingOrder, ledgerEntry)
    }
}