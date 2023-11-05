package com.account_balancer.repositories

import com.account_balancer.db.Tables.MONEY_BOOKING_ORDER
import com.account_balancer.db.tables.records.MoneyBookingOrderRecord
import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.MoneyBookingId
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.services.Pagination
import com.account_balancer.util.NotFoundException
import java.time.LocalDateTime
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class MoneyBookingOrdersRepository(
    protected val jooq: DSLContext,
) {
    fun insert(moneyBookingOrderEntity: MoneyBookingOrderEntity): MoneyBookingOrderEntity {
        return jooq.insertInto(MONEY_BOOKING_ORDER)
            .set(moneyBookingOrderEntity.toRecord())
            .returning()
            .fetchOne()!!
            .toEntity()
    }

    private fun findById(moneyBookingId: MoneyBookingId): MoneyBookingOrderEntity? {
        return jooq.selectFrom(MONEY_BOOKING_ORDER)
            .where(MONEY_BOOKING_ORDER.ID.eq(moneyBookingId))
            .fetchOne()
            ?.toEntity()
    }

    fun requireById(moneyBookingId: MoneyBookingId): MoneyBookingOrderEntity {
        return findById(moneyBookingId)
            ?: throw NotFoundException("Money booking order with id $moneyBookingId not found")
    }

    fun updateStatusAndLedgerUpdatedAt(
        checkoutId: CheckoutId,
        moneyBookingStatus: MoneyBookingStatus,
        ledgerCreatedAt: LocalDateTime
    ): MoneyBookingOrderEntity {
        return jooq.update(MONEY_BOOKING_ORDER)
            .set(MONEY_BOOKING_ORDER.STATUS, moneyBookingStatus.name)
            .set(MONEY_BOOKING_ORDER.LEDGER_UPDATED, ledgerCreatedAt)
            .where(MONEY_BOOKING_ORDER.CHECKOUT_ID.eq(checkoutId))
            .returning()
            .fetchOne()!!
            .toEntity()
    }


    //    TODO implement pagination using seek method in order to use index scanning in case of performance issues https://vladmihalcea.com/sql-seek-keyset-pagination/
    fun findBy(
        customerId: AccountId?,
        tenantId: AccountId?,
        status: MoneyBookingStatus?,
        createAfter: LocalDateTime?,
        createBefore: LocalDateTime?,
        pagination: Pagination?,
    ): List<MoneyBookingOrderEntity> {
        return jooq.selectFrom(MONEY_BOOKING_ORDER)
            .where(
                customerId?.let { MONEY_BOOKING_ORDER.CUSTOMER_ID.eq(it) },
                tenantId?.let { MONEY_BOOKING_ORDER.TENANT_ID.eq(it) },
                status?.let { MONEY_BOOKING_ORDER.STATUS.eq(it.name) },
                createAfter?.let { MONEY_BOOKING_ORDER.CREATED_AT.gt(it) },
                createBefore?.let { MONEY_BOOKING_ORDER.CREATED_AT.lt(it) },
            )
            .orderBy(MONEY_BOOKING_ORDER.CREATED_AT.desc())
            .apply {
                if (pagination != null) {
                    this.limit(pagination.limit).offset(pagination.offset)
                }
            }
            .fetch()
            .map { it.toEntity() }
    }

    private fun MoneyBookingOrderRecord.toEntity(): MoneyBookingOrderEntity {
        return MoneyBookingOrderEntity(
            id = this.id,
            checkoutId = this.checkoutId,
            customerId = this.customerId,
            tenantId = this.tenantId,
            status = MoneyBookingStatus.valueOf(this.status),
            amount = this.amount,
            currencyCode = this.currencyCode,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ledgerUpdatedAt = this.ledgerUpdated
        )
    }

    private fun MoneyBookingOrderEntity.toRecord(): MoneyBookingOrderRecord {
        return MoneyBookingOrderRecord(
            this.id,
            this.checkoutId,
            this.customerId,
            this.tenantId,
            this.status.name,
            this.amount,
            this.currencyCode,
            this.createdAt,
            this.updatedAt,
            this.ledgerUpdatedAt
        )
    }
}