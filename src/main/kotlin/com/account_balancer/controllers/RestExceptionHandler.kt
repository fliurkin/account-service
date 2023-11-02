package com.account_balancer.controllers

import com.account_balancer.util.InvalidBalanceFormatException
import com.account_balancer.util.InvalidUuidFormatException
import com.account_balancer.util.NotFoundException
import java.time.LocalDateTime
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(InvalidBalanceFormatException::class)
    protected fun handleInvalidBalanceFormatException(ex: InvalidBalanceFormatException): ResponseEntity<Any> {
        val apiError = ApiError(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid balance format", LocalDateTime.now())
        return buildResponseEntity(apiError)
    }

    @ExceptionHandler(InvalidUuidFormatException::class)
    protected fun handleInvalidUuidFormatException(ex: InvalidUuidFormatException): ResponseEntity<Any> {
        val apiError = ApiError(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid UUID format", LocalDateTime.now())
        return buildResponseEntity(apiError)
    }

    @ExceptionHandler(NotFoundException::class)
    protected fun handleNotFoundException(ex: NotFoundException): ResponseEntity<Any> {
        val apiError = ApiError(HttpStatus.NOT_FOUND, ex.message ?: "Entity not found", LocalDateTime.now())
        return buildResponseEntity(apiError)
    }

    private fun buildResponseEntity(apiError: ApiError): ResponseEntity<Any> {
        return ResponseEntity(apiError, apiError.status)
    }
}

data class ApiError(
    val status: HttpStatus,
    val message: String,
    val timestamp: LocalDateTime
)
