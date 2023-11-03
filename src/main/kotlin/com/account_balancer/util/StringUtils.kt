package com.account_balancer.util

import java.math.BigDecimal
import java.util.UUID

fun String.toBigDecimalOrThrow(): BigDecimal {
    return this.let {
        try {
            this.toBigDecimal()
        } catch (e: NumberFormatException) {
            throw InvalidBalanceFormatException(this)
        }
    }
}

fun String.toUuidOrThrow(): UUID {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        throw InvalidUuidFormatException(this)
    }
}