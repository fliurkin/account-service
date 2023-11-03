package com.account_balancer.repositories

import com.account_balancer.db.Tables.ACCOUNT
import com.account_balancer.db.Tables.LEDGER_ENTRY
import com.account_balancer.db.tables.records.LedgerEntryRecord
import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.LedgerAccountBalance
import com.account_balancer.models.LedgerEntryEntity
import java.math.BigDecimal
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.jooq.impl.DSL.neg
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Repository

@Repository
class LedgerEntriesRepository(
    private val jooq: DSLContext,
) {
    fun insert(ledgerEntryEntity: LedgerEntryEntity): LedgerEntryEntity {
        return jooq.insertInto(LEDGER_ENTRY)
            .set(ledgerEntryEntity.toRecord())
            .returning()
            .fetchOne()!!
            .toEntity()
    }

    fun findBy(checkoutId: CheckoutId, credit: AccountId, debit: AccountId): LedgerEntryEntity? {
        return jooq.selectFrom(LEDGER_ENTRY)
            .where(LEDGER_ENTRY.CHECKOUT_ID.eq(checkoutId))
            .and(LEDGER_ENTRY.CREDIT.eq(credit))
            .and(LEDGER_ENTRY.DEBIT.eq(debit))
            .fetchOne()
            ?.toEntity()
    }

    fun getAccountBalances(credit: AccountId, debit: AccountId): List<LedgerAccountBalance> {
        val ledgerEntryId = DSL.field("ledger_entry_id", UUID::class.java)
        val ledgerEntryAccountId = DSL.field("ledger_entry_account_id", AccountId::class.java)
        val ledgerEntryAmount = DSL.field("ledger_entry_amount", BigDecimal::class.java)

        val accountLedgersCte =
            getAccountLedgersCte(ledgerEntryId, ledgerEntryAccountId, ledgerEntryAmount, credit, debit)

        val coalesceSum = DSL.coalesce(DSL.sum(ledgerEntryAmount), BigDecimal.ZERO)
        return jooq.with(accountLedgersCte)
            .select(ACCOUNT.ID, coalesceSum.`as`("balance")).from(ACCOUNT)
            .leftJoin(accountLedgersCte).on(ACCOUNT.ID.eq(ledgerEntryAccountId))
            .groupBy(ACCOUNT.ID)
            .fetch()
            .map { (accountId, balance) ->
                LedgerAccountBalance(
                    accountId = accountId,
                    balance = balance,
                )
            }
    }

    private fun getAccountLedgersCte(
        ledgerEntryId: Field<UUID>,
        ledgerEntryAccountId: Field<AccountId>,
        ledgerEntryAmount: Field<BigDecimal>,
        credit: AccountId,
        debit: AccountId
    ) = DSL.name("account_ledgers").`as`(
        select(
            LEDGER_ENTRY.ID.`as`(ledgerEntryId),
            LEDGER_ENTRY.CREDIT.`as`(ledgerEntryAccountId),
            neg(LEDGER_ENTRY.AMOUNT).`as`(ledgerEntryAmount)
        )
            .from(LEDGER_ENTRY)
            .where(LEDGER_ENTRY.CREDIT.eq(credit))
            .unionAll(
                select(
                    LEDGER_ENTRY.ID.`as`(ledgerEntryId),
                    LEDGER_ENTRY.DEBIT.`as`(ledgerEntryAccountId),
                    LEDGER_ENTRY.AMOUNT.`as`(ledgerEntryAmount)
                )
                    .from(LEDGER_ENTRY)
                    .where(LEDGER_ENTRY.DEBIT.eq(debit))
            )
    )

    private fun LedgerEntryEntity.toRecord(): LedgerEntryRecord {
        return LedgerEntryRecord(
            this.id,
            this.amount,
            this.checkoutId,
            this.credit,
            this.debit,
            null,
            null
        )
    }

    private fun LedgerEntryRecord.toEntity(): LedgerEntryEntity {
        return LedgerEntryEntity(
            id = this.id,
            amount = this.amount,
            checkoutId = this.checkoutId,
            credit = this.credit,
            debit = this.debit,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }
}