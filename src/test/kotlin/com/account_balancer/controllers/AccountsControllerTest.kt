package com.account_balancer.controllers

import com.account_balancer.BaseTest
import com.account_balancer.repositories.AccountsRepository
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.UUID
import net.javacrumbs.jsonunit.assertj.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AccountsControllerTest : BaseTest() {

    @Autowired
    private lateinit var accountsRepository: AccountsRepository

    @Test
    fun `POST accounts should successfully create account`() {
//        given
        val createAccountRequest = CreateAccountRequest(
            accountId = UUID.randomUUID().toString(),
            balance = "100.00",
        )

//        when
        val responseString = mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.valueToTree<ObjectNode>(createAccountRequest)
        }
//        then
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

        val createAccountResponse = objectMapper.readValue<CreateAccountResponse>(responseString)

        val account = accountsRepository.findById(createAccountResponse.accountId)
        account shouldNotBe null
        account?.balance?.toString() shouldBe createAccountRequest.balance
    }

    @Test
    fun `POST accounts should respond with 400 error code and specific message when accountId format is invalid`() {
//        given
        val createAccountRequest = CreateAccountRequest(
            accountId = "bla bla",
            balance = "100.00",
        )

//        when
        val responseString = mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.valueToTree<ObjectNode>(createAccountRequest)
        }
//        then
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response.contentAsString

        assertThatJson(responseString)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .inPath("message")
            .isString
            .isEqualTo("Invalid UUID format: bla bla")
    }

    @Test
    fun `POST accounts should respond with 400 error code and specific message when balance format is invalid`() {
//        given
        val createAccountRequest = CreateAccountRequest(
            accountId = UUID.randomUUID().toString(),
            balance = "bla bla",
        )

//        when
        val responseString = mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.valueToTree<ObjectNode>(createAccountRequest)
        }
//        then
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .response.contentAsString

        assertThatJson(responseString)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .inPath("message")
            .isString
            .isEqualTo("Invalid balance format: bla bla")
    }

    @Test
    fun `GET accounts_{accountId} should sucessfully return account balance`() {
//        given
        val setupAccount = setupUtils.setupAccount()

//        when
        val responseString = mockMvc.get("/accounts/${setupAccount.id}/balance")
//        then
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

        assertThatJson(responseString)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """
                {
                    "balance": "${setupAccount.balance}",
                    "currencyCode": "EUR"
                }
                """.trimIndent()
            )
    }
}