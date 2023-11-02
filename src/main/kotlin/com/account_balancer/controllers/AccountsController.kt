package com.account_balancer.controllers

import com.account_balancer.models.AccountEntity
import com.account_balancer.models.AccountId
import com.account_balancer.services.AccountService
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountsController(
    private val accountsService: AccountService
) {
    @PostMapping("/accounts")
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<CreateAccountResponse> {
        val createdEntity = accountsService.createAccountWithBalance(request.accountId, request.balance)
        return ResponseEntity.ok(CreateAccountResponse.from(createdEntity))
    }

    @GetMapping("/accounts/{accountId}/balance")
    fun getAccountBalance(@PathVariable("accountId") accountId: AccountId): ResponseEntity<GetAccountBalanceResponse> {
        val accountEntity = accountsService.getAccountEntity(accountId)
        return ResponseEntity.ok(GetAccountBalanceResponse.from(accountEntity))
    }
}

data class CreateAccountRequest(
    val accountId: String,
    val balance: String? = null,
)

data class CreateAccountResponse(
    val accountId: UUID,
    val balance: String,
    val currencyCode: String
) {
    companion object {
        fun from(accountEntity: AccountEntity): CreateAccountResponse {
            return CreateAccountResponse(
                accountId = accountEntity.id,
                balance = accountEntity.balance.toString(),
                currencyCode = accountEntity.currencyCode,
            )
        }
    }
}

data class GetAccountBalanceResponse(
    val balance: String,
    val currencyCode: String,
) {
    companion object {
        fun from(accountEntity: AccountEntity): GetAccountBalanceResponse {
            return GetAccountBalanceResponse(
                balance = accountEntity.balance.toString(),
                currencyCode = accountEntity.currencyCode,
            )
        }
    }
}