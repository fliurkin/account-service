package com.account_balancer.controllers

import com.account_balancer.models.AccountId
import com.account_balancer.models.CheckoutId
import com.account_balancer.models.MoneyBookingId
import com.account_balancer.models.MoneyBookingOrderEntity
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.services.MoneyTransactionsService
import com.account_balancer.util.toBigDecimalOrThrow
import com.account_balancer.util.toUuidOrThrow
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/money-transaction")
//todo Provide a means for a customer-care service to fetch recent transactions per customer and tenant
//todo create audit log
class MoneyTransactionsController(
    private val moneyTransactionsService: MoneyTransactionsService
) {
    @PostMapping
    fun createMoneyBookingOrder(@RequestBody request: CreateMoneyBookingTransactionRequest): ResponseEntity<MoneyBookingTransactionResponse> {
        val moneyBookingOrderEntity = moneyTransactionsService.bookMoney(
            checkoutId = request.checkoutId.toUuidOrThrow(),
            customerId = request.customerId.toUuidOrThrow(),
            tenantId = request.tenantId.toUuidOrThrow(),
            amount = request.amount.toBigDecimalOrThrow(),
        )
        return ResponseEntity.ok(MoneyBookingTransactionResponse.from(moneyBookingOrderEntity))
    }

    @PostMapping("/{moneyBookingId}/cancel")
    fun cancelMoneyBookingOrder(@PathVariable("moneyBookingId") moneyBookingId: MoneyBookingId): ResponseEntity<MoneyBookingTransactionResponse> {
        val moneyBookingOrderEntity = moneyTransactionsService.cancelMoneyBooking(moneyBookingId)
        return ResponseEntity.ok(MoneyBookingTransactionResponse.from(moneyBookingOrderEntity))
    }
}

data class CreateMoneyBookingTransactionRequest(
    val checkoutId: String,
    val customerId: String,
    val tenantId: String,
    val amount: String,
)

data class MoneyBookingTransactionResponse(
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
        fun from(moneyBookingOrderEntity: MoneyBookingOrderEntity) = MoneyBookingTransactionResponse(
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