package com.account_balancer.services

import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.LedgerEntryEntity
import com.account_balancer.models.MoneyBookingId
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.repositories.MoneyBookingOrdersRepository
import java.math.BigDecimal
import org.springframework.stereotype.Service

@Service
class MoneyBookingOrdersService(
    private val moneyBookingOrdersRepository: MoneyBookingOrdersRepository
) {
    fun createMoneyBookingOrder(
        checkoutId: CheckoutId,
        customerId: AccountId,
        tenantId: AccountId,
        amount: BigDecimal
    ): MoneyBookingOrderEntity {
        return moneyBookingOrdersRepository.insert(
            MoneyBookingOrderEntity.of(checkoutId, customerId, tenantId, amount)
        )
    }

    fun getMoneyBookingOrder(moneyBookingId: MoneyBookingId): MoneyBookingOrderEntity {
        return moneyBookingOrdersRepository.requireById(moneyBookingId)
    }


    fun completeMoneyBookingOrder(
        moneyBookingEntity: MoneyBookingOrderEntity,
        ledgerEntity: LedgerEntryEntity
    ): MoneyBookingOrderEntity {
        return moneyBookingOrdersRepository.updateStatusAndLedgerUpdatedAt(
            moneyBookingEntity.checkoutId,
            MoneyBookingStatus.SUCCESS,
            ledgerEntity.createdAt
        )
    }

    fun cancelMoneyBookingOrder(
        moneyBookingEntity: MoneyBookingOrderEntity,
        ledgerEntity: LedgerEntryEntity
    ): MoneyBookingOrderEntity {
        return moneyBookingOrdersRepository.updateStatusAndLedgerUpdatedAt(
            moneyBookingEntity.checkoutId,
            MoneyBookingStatus.CANCELLED,
            ledgerEntity.createdAt
        )
    }
}