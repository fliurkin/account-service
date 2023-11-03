package com.account_balancer.controllers

import com.account_balancer.BaseTest
import com.account_balancer.models.LedgerAccountBalance
import com.account_balancer.models.MoneyBookingStatus
import com.account_balancer.repositories.AccountsRepository
import com.account_balancer.repositories.LedgerEntriesRepository
import com.account_balancer.repositories.MoneyBookingOrdersRepository
import com.account_balancer.test_utils.SetupUtils.JsonConstants.jsonUnitIgnoreElement
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

        assertThatJson(responseString).isEqualTo(
            """
            {
                "checkoutId": "$givenCheckoutId",
                "customerId": "${setupAccount.id}",
                "tenantId": "${setupAccount2.id}",
                "status": "SUCCESS",
                "amount": "100.00",
                "currencyCode": "EUR",
                "createdAt": "$jsonUnitIgnoreElement",
                "ledgerUpdatedAt": "$jsonUnitIgnoreElement"
            }
        """.trimIndent()
        )

        accountsRepository.requiredById(setupAccount.id).balance shouldBe BigDecimal("-100.00")
        accountsRepository.requiredById(setupAccount2.id).balance shouldBe BigDecimal("100.00")

        ledgerEntriesRepository.getAccountBalances(
            setupAccount.id,
            setupAccount2.id
        ) shouldContainExactlyInAnyOrder listOf(
            LedgerAccountBalance(setupAccount.id, BigDecimal("-100.00")),
            LedgerAccountBalance(setupAccount2.id, BigDecimal("100.00"))
        )
        val ledgerEntryEntity = ledgerEntriesRepository.findBy(givenCheckoutId, setupAccount.id, setupAccount2.id)
        ledgerEntryEntity shouldNotBe null

        val moneyBookingOrderEntity = moneyBookingOrdersRepository.requireById(givenCheckoutId)
        moneyBookingOrderEntity.status shouldBe MoneyBookingStatus.SUCCESS
        moneyBookingOrderEntity.ledgerUpdatedAt shouldBe ledgerEntryEntity!!.createdAt
    }
}