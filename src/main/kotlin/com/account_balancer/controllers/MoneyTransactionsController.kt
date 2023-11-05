package com.account_balancer.controllers

import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.MoneyBookingId
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.services.MoneyBookingOrderQuery
import com.account_balancer.services.MoneyBookingOrdersService
import com.account_balancer.services.MoneyTransactionsService
import com.account_balancer.services.Pagination
import com.account_balancer.util.toBigDecimalOrThrow
import com.account_balancer.util.toUuidOrThrow
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/money-transactions")
class MoneyTransactionsController(
    private val moneyTransactionsService: MoneyTransactionsService,
    private val moneyBookingOrdersService: MoneyBookingOrdersService,
) {
    @PostMapping
    fun createMoneyBookingOrder(@RequestBody request: CreateMoneyBookingTransactionRequest): ResponseEntity<MoneyBookingTransactionModel> {
        val moneyBookingOrderEntity = moneyTransactionsService.bookMoney(
            checkoutId = request.checkoutId.toUuidOrThrow(),
            customerId = request.customerId.toUuidOrThrow(),
            tenantId = request.tenantId.toUuidOrThrow(),
            amount = request.amount.toBigDecimalOrThrow(),
        )
        return ResponseEntity.ok(MoneyBookingTransactionModel.from(moneyBookingOrderEntity))
    }

    @PostMapping("/{moneyBookingId}/cancel")
    fun cancelMoneyBookingOrder(@PathVariable("moneyBookingId") moneyBookingId: MoneyBookingId): ResponseEntity<MoneyBookingTransactionModel> {
        val moneyBookingOrderEntity = moneyTransactionsService.cancelMoneyBooking(moneyBookingId)
        return ResponseEntity.ok(MoneyBookingTransactionModel.from(moneyBookingOrderEntity))
    }

    @GetMapping
    fun getMoneyBookingOrders(
        @RequestParam("customerId", required = false) customerId: AccountId?,
        @RequestParam("tenantId", required = false) tenantId: AccountId?,
        @RequestParam("status", required = false) status: MoneyBookingStatus?,
        @RequestParam("createdAfter", required = false) createdAfter: LocalDateTime?,
        @RequestParam("createdBefore", required = false) createdBefore: LocalDateTime?,
        pagination: Pagination
    ): ResponseEntity<MoneyBookingTransactions> {
        val moneyBookingOrderEntities = moneyBookingOrdersService.getMoneyBookingOrders(
            MoneyBookingOrderQuery(
                customerId = customerId,
                tenantId = tenantId,
                status = status,
                createAfter = createdAfter,
                createBefore = createdBefore,
                pagination = pagination
            )
        )
            .map { MoneyBookingTransactionModel.from(it) }
        return ResponseEntity.ok(MoneyBookingTransactions(moneyBookingOrderEntities))
    }
}

data class CreateMoneyBookingTransactionRequest(
    val checkoutId: String,
    val customerId: String,
    val tenantId: String,
    val amount: String,
)

data class MoneyBookingTransactions(
    val transactions: List<MoneyBookingTransactionModel>
)

data class MoneyBookingTransactionModel(
    val moneyBookingOrderId: MoneyBookingId,
    val checkoutId: CheckoutId,
    val customerId: AccountId,
    val tenantId: AccountId,
    val status: MoneyBookingStatus,
    val amount: String,
    val currencyCode: String,
    val createdAt: LocalDateTime,
    val ledgerUpdatedAt: LocalDateTime,
) {
    companion object {
        fun from(moneyBookingOrderEntity: MoneyBookingOrderEntity) = MoneyBookingTransactionModel(
            moneyBookingOrderId = moneyBookingOrderEntity.id,
            checkoutId = moneyBookingOrderEntity.checkoutId,
            customerId = moneyBookingOrderEntity.customerId,
            tenantId = moneyBookingOrderEntity.tenantId,
            status = moneyBookingOrderEntity.status,
            amount = moneyBookingOrderEntity.amount.toString(),
            currencyCode = moneyBookingOrderEntity.currencyCode,
            createdAt = moneyBookingOrderEntity.createdAt,
            ledgerUpdatedAt = moneyBookingOrderEntity.ledgerUpdatedAt
                ?: throw IllegalStateException("Ledger updated at is null")
        )
    }
}