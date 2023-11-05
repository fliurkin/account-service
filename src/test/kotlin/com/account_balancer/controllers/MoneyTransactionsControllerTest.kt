package com.account_balancer.controllers

import com.account_balancer.BaseTest
import com.account_balancer.models.LedgerAccountBalance
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.repositories.AccountsRepository
import com.account_balancer.repositories.LedgerEntriesRepository
import com.account_balancer.repositories.MoneyBookingOrdersRepository
import com.account_balancer.test_utils.SetupUtils.JsonConstants.jsonUnitIgnoreElement
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post


class MoneyTransactionsControllerTest : BaseTest() {

    @Autowired
    private lateinit var accountsRepository: AccountsRepository

    @Autowired
    private lateinit var moneyBookingOrdersRepository: MoneyBookingOrdersRepository

    @Autowired
    private lateinit var ledgerEntriesRepository: LedgerEntriesRepository

    @Test
    fun `POST money-transactions should successfully create money booking order, update ledger and balances`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()
        val givenCheckoutId = UUID.randomUUID()

        // when
        val responseString = mockMvc.post("/money-transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "checkoutId": "$givenCheckoutId",
                    "customerId": "${setupAccount.id}",
                    "tenantId": "${setupAccount2.id}",
                    "amount": "100.00"
                }
            """.trimIndent()
        }
            // then
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

        val moneyBookingResponse = objectMapper.readValue<MoneyBookingTransactionModel>(responseString)
        moneyBookingResponse.checkoutId shouldBe givenCheckoutId
        moneyBookingResponse.customerId shouldBe setupAccount.id
        moneyBookingResponse.tenantId shouldBe setupAccount2.id
        moneyBookingResponse.status shouldBe MoneyBookingStatus.SUCCESS
        moneyBookingResponse.amount shouldBe "100.00"
        moneyBookingResponse.currencyCode shouldBe "EUR"
        moneyBookingResponse.createdAt shouldNotBe null
        moneyBookingResponse.ledgerUpdatedAt shouldNotBe null

        accountsRepository.requiredById(setupAccount.id).balance shouldBe BigDecimal("-100.00")
        accountsRepository.requiredById(setupAccount2.id).balance shouldBe BigDecimal("100.00")

        ledgerEntriesRepository.getAccountBalances(
            setupAccount.id,
            setupAccount2.id
        ) shouldContainExactlyInAnyOrder listOf(
            LedgerAccountBalance(setupAccount.id, BigDecimal("-100.00")),
            LedgerAccountBalance(setupAccount2.id, BigDecimal("100.00"))
        )
        val ledgerEntryEntity =
            ledgerEntriesRepository.findBy(
                moneyBookingResponse.moneyBookingOrderId,
                setupAccount.id,
                setupAccount2.id,
                BigDecimal("100.00")
            )
        ledgerEntryEntity shouldNotBe null

        val moneyBookingOrderEntity = moneyBookingOrdersRepository.requireById(moneyBookingResponse.moneyBookingOrderId)
        moneyBookingOrderEntity.status shouldBe MoneyBookingStatus.SUCCESS
        moneyBookingOrderEntity.ledgerUpdatedAt shouldBe ledgerEntryEntity!!.createdAt
    }

    @Test
    fun `POST money-transactions_{checkoutId}_cancel should CANCEL money booking order, and run reversed money transaction`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()
        val (moneyBookingOrderEntity, _, _) = setupUtils.setupMoneyBooking(
            checkoutId = UUID.randomUUID(),
            customerId = setupAccount.id,
            tenantId = setupAccount2.id,
            amount = BigDecimal("500.00")
        )

        // when
        val responseString = mockMvc.post("/money-transactions/${moneyBookingOrderEntity.id}/cancel") {
            contentType = MediaType.APPLICATION_JSON
        }
            // then
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

        assertThatJson(responseString).isEqualTo(
            """
            {
                "moneyBookingOrderId": "${moneyBookingOrderEntity.id}",
                "checkoutId": "${moneyBookingOrderEntity.checkoutId}",
                "customerId": "${moneyBookingOrderEntity.customerId}",
                "tenantId": "${moneyBookingOrderEntity.tenantId}",
                "status": "CANCELLED",
                "amount": "500.00",
                "currencyCode": "EUR",
                "createdAt": "$jsonUnitIgnoreElement",
                "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
            }
            """.trimIndent()
        )

        accountsRepository.requiredById(setupAccount.id).balance shouldBe BigDecimal("0.00")
        accountsRepository.requiredById(setupAccount2.id).balance shouldBe BigDecimal("0.00")

        ledgerEntriesRepository.getAccountBalances(
            setupAccount.id,
            setupAccount2.id
        ) shouldContainExactlyInAnyOrder listOf(
            LedgerAccountBalance(setupAccount.id, BigDecimal("0.00")),
            LedgerAccountBalance(setupAccount2.id, BigDecimal("0.00"))
        )
        val newLedgerEntryEntity = ledgerEntriesRepository.findBy(
            moneyBookingOrderEntity.id,
            setupAccount.id,
            setupAccount2.id,
            BigDecimal("-500.00")
        )
        newLedgerEntryEntity shouldNotBe null
        newLedgerEntryEntity!!.createdAt shouldBe newLedgerEntryEntity.createdAt

        val updatedMoneyBookingOrderEntity = moneyBookingOrdersRepository.requireById(moneyBookingOrderEntity.id)
        updatedMoneyBookingOrderEntity.status shouldBe MoneyBookingStatus.CANCELLED
        updatedMoneyBookingOrderEntity.ledgerUpdatedAt shouldBe newLedgerEntryEntity.createdAt
    }

    @Test
    fun `GET money-transactions should return all money booking orders by customer id`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()
        val setupAccount3 = setupUtils.setupAccount()
        val (moneyBookingOrderEntity, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount.id, setupAccount2.id, BigDecimal("500.00"))
        val (moneyBookingOrderEntity2, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount.id, setupAccount2.id, BigDecimal("500.00"))
        val (_, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount3.id, setupAccount.id, BigDecimal("500.00"))

        // when
        val responseString = mockMvc.get("/money-transactions?customerId=${setupAccount.id}&limit=100&offset=0") {
            contentType = MediaType.APPLICATION_JSON
        }
            // then
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

        assertThatJson(responseString).isEqualTo(
            """
            {
                "transactions": [
                    {
                        "moneyBookingOrderId": "${moneyBookingOrderEntity2.id}",
                        "checkoutId": "${moneyBookingOrderEntity2.checkoutId}",
                        "customerId": "${moneyBookingOrderEntity2.customerId}",
                        "tenantId": "${moneyBookingOrderEntity2.tenantId}",
                        "status": "SUCCESS",
                        "amount": "500.00",
                        "currencyCode": "EUR",
                        "createdAt": "$jsonUnitIgnoreElement",
                        "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
                    },
                    {
                        "moneyBookingOrderId": "${moneyBookingOrderEntity.id}",
                        "checkoutId": "${moneyBookingOrderEntity.checkoutId}",
                        "customerId": "${moneyBookingOrderEntity.customerId}",
                        "tenantId": "${moneyBookingOrderEntity.tenantId}",
                        "status": "SUCCESS",
                        "amount": "500.00",
                        "currencyCode": "EUR",
                        "createdAt": "$jsonUnitIgnoreElement",
                        "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
                    }
                ]   
            }
            """.trimIndent()
        )
    }

    @Test
    fun `GET money-transactions should return all money booking orders by tenant id`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()
        val setupAccount3 = setupUtils.setupAccount()
        val (moneyBookingOrderEntity, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount.id, setupAccount2.id, BigDecimal("500.00"))
        val (moneyBookingOrderEntity2, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount.id, setupAccount2.id, BigDecimal("500.00"))
        val (_, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount3.id, setupAccount.id, BigDecimal("500.00"))

        // when
        val responseString = mockMvc.get("/money-transactions?tenantId=${setupAccount2.id}&limit=100&offset=0") {
            contentType = MediaType.APPLICATION_JSON
        }
            // then
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

        assertThatJson(responseString).isEqualTo(
            """
            {
                "transactions": [
                    {
                        "moneyBookingOrderId": "${moneyBookingOrderEntity2.id}",
                        "checkoutId": "${moneyBookingOrderEntity2.checkoutId}",
                        "customerId": "${moneyBookingOrderEntity2.customerId}",
                        "tenantId": "${moneyBookingOrderEntity2.tenantId}",
                        "status": "SUCCESS",
                        "amount": "500.00",
                        "currencyCode": "EUR",
                        "createdAt": "$jsonUnitIgnoreElement",
                        "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
                    },
                    {
                        "moneyBookingOrderId": "${moneyBookingOrderEntity.id}",
                        "checkoutId": "${moneyBookingOrderEntity.checkoutId}",
                        "customerId": "${moneyBookingOrderEntity.customerId}",
                        "tenantId": "${moneyBookingOrderEntity.tenantId}",
                        "status": "SUCCESS",
                        "amount": "500.00",
                        "currencyCode": "EUR",
                        "createdAt": "$jsonUnitIgnoreElement",
                        "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
                    }
                ]   
            }
            """.trimIndent()
        )
    }

    @Test
    fun `GET money-transactions should return all money booking orders by createdAfter and createdBefore properties`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()

        val setupAccount3 = setupUtils.setupAccount()
        val (_, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount3.id, setupAccount.id, BigDecimal("500.00"))

        val after = LocalDateTime.now()
        val (moneyBookingOrderEntity, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount.id, setupAccount2.id, BigDecimal("500.00"))
        val (moneyBookingOrderEntity2, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount.id, setupAccount2.id, BigDecimal("500.00"))
        val before = LocalDateTime.now()

        val (_, _, _) = setupUtils.setupMoneyBooking(UUID.randomUUID(), setupAccount3.id, setupAccount.id, BigDecimal("500.00"))

        // when
        val responseString =
            mockMvc.get("/money-transactions?createdAfter=$after&createdBefore=$before&limit=100&offset=0") {
                contentType = MediaType.APPLICATION_JSON
            }
                // then
                .andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString

        assertThatJson(responseString).isEqualTo(
            """
            {
                "transactions": [
                    {
                        "moneyBookingOrderId": "${moneyBookingOrderEntity2.id}",
                        "checkoutId": "${moneyBookingOrderEntity2.checkoutId}",
                        "customerId": "${moneyBookingOrderEntity2.customerId}",
                        "tenantId": "${moneyBookingOrderEntity2.tenantId}",
                        "status": "SUCCESS",
                        "amount": "500.00",
                        "currencyCode": "EUR",
                        "createdAt": "$jsonUnitIgnoreElement",
                        "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
                    },
                    {
                        "moneyBookingOrderId": "${moneyBookingOrderEntity.id}",
                        "checkoutId": "${moneyBookingOrderEntity.checkoutId}",
                        "customerId": "${moneyBookingOrderEntity.customerId}",
                        "tenantId": "${moneyBookingOrderEntity.tenantId}",
                        "status": "SUCCESS",
                        "amount": "500.00",
                        "currencyCode": "EUR",
                        "createdAt": "$jsonUnitIgnoreElement",
                        "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
                    }
                ]   
            }
            """.trimIndent()
        )
    }
}