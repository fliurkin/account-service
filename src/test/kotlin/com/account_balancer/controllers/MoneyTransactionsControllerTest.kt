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
import java.util.UUID
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post


class MoneyTransactionsControllerTest : BaseTest() {

    @Autowired
    private lateinit var accountsRepository: AccountsRepository

    @Autowired
    private lateinit var moneyBookingOrdersRepository: MoneyBookingOrdersRepository

    @Autowired
    private lateinit var ledgerEntriesRepository: LedgerEntriesRepository

    @Test
    fun `POST money-transaction should successfully create money booking order, update ledger and balances`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()
        val givenCheckoutId = UUID.randomUUID()

        // when
        val responseString = mockMvc.post("/money-transaction") {
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

        val moneyBookingResponse = objectMapper.readValue<MoneyBookingTransactionResponse>(responseString)
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
    fun `POST money-transaction_{checkoutId}_cancel should CANCEL money booking order, and run reversed money transaction`() {
        // given
        val setupAccount = setupUtils.setupAccount()
        val setupAccount2 = setupUtils.setupAccount()
        val (moneyBookingOrderEntity, ledgerEntryEntity, accountEntities) = setupUtils.setupMoneyBooking(
            checkoutId = UUID.randomUUID(),
            customerId = setupAccount.id,
            tenantId = setupAccount2.id,
            amount = BigDecimal("500.00")
        )

        // when
        val responseString = mockMvc.post("/money-transaction/${moneyBookingOrderEntity.id}/cancel") {
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
}