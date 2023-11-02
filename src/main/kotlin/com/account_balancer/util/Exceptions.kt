package com.account_balancer.util

class InvalidBalanceFormatException(balance: String) : RuntimeException("Invalid balance format: $balance")
class InvalidUuidFormatException(uuid: String) : RuntimeException("Invalid UUID format: $uuid")
class NotFoundException(message: String) : RuntimeException(message)