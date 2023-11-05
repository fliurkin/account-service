package com.account_balancer.services

import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.LedgerEntryEntity
import com.account_balancer.models.MoneyBookingId
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.repositories.MoneyBookingOrdersRepository
import java.math.BigDecimal
import java.time.LocalDateTime
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

    fun getMoneyBookingOrders(moneyBookingOrderQuery: MoneyBookingOrderQuery): List<MoneyBookingOrderEntity> {
        return moneyBookingOrdersRepository.findBy(
            moneyBookingOrderQuery.customerId,
            moneyBookingOrderQuery.tenantId,
            moneyBookingOrderQuery.status,
            moneyBookingOrderQuery.createAfter,
            moneyBookingOrderQuery.createBefore,
        )
    }
}

data class MoneyBookingOrderQuery(
    val customerId: AccountId? = null,
    val tenantId: AccountId? = null,
    val status: MoneyBookingStatus? = null,
    val createAfter: LocalDateTime? = null,
    val createBefore: LocalDateTime? = null,
)