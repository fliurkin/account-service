package com.account_balancer.repositories

import com.account_balancer.db.tables.Account.ACCOUNT
import com.account_balancer.db.tables.records.AccountRecord
import com.account_balancer.models.AccountEntity
import com.account_balancer.models.AccountId
import com.account_balancer.models.LedgerAccountBalance
import com.account_balancer.util.NotFoundException
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class AccountsRepository(
    private val jooq: DSLContext,
) {
    fun insert(accountEntity: AccountEntity): AccountEntity {
        return jooq.insertInto(ACCOUNT)
            .set(accountEntity.toRecord())
            .returning()
            .fetchOne()!!
            .toEntity()
    }

    fun updateAllAccountBalances(accountBalances: List<LedgerAccountBalance>) {
        if (accountBalances.isEmpty()) return
        jooq.batched {
            accountBalances.forEach { balance ->
                it.dsl().update(ACCOUNT)
                    .set(ACCOUNT.BALANCE, ACCOUNT.BALANCE.plus(balance.balance))
                    .where(ACCOUNT.ID.eq(balance.accountId))
                    .execute()
            }
        }
    }

    fun findById(accountId: AccountId): AccountEntity? {
        return jooq.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(accountId))
            .fetchOne()
            ?.toEntity()
    }

    fun requiredById(accountId: AccountId): AccountEntity {
        return findById(accountId) ?: throw NotFoundException("Account with id $accountId not found")
    }

    private fun AccountEntity.toRecord(): AccountRecord {
        return AccountRecord(
            this.id,
            this.balance,
            this.currencyCode,
            this.createdAt,
            this.updatedAt,
        )
    }

    private fun AccountRecord.toEntity(): AccountEntity {
        return AccountEntity(
            id = this.id,
            balance = this.balance,
            currencyCode = this.currencyCode,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }
}