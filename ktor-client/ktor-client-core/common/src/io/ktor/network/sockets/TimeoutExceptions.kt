/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.util.*
import io.ktor.utils.io.errors.*

/**
 * This exception is thrown in case connect timeout exceeded.
 */
expect class ConnectTimeoutException(request: HttpRequestData) : IOException

/**
 * This exception is thrown in case socket timeout (read or write) exceeded.
 */
expect class SocketTimeoutException(request: HttpRequestData) : IOException

/**
 * Convert long timeout in milliseconds to int value. To do that we need to consider [HttpTimeout.INFINITE_TIMEOUT_MS]
 * as zero and convert timeout value to [Int].
 */
@InternalAPI
fun convertLongTimeoutToIntWithInfiniteAsZero(timeout: Long): Int = when {
    timeout == HttpTimeout.INFINITE_TIMEOUT_MS -> 0
    timeout < Int.MIN_VALUE -> Int.MIN_VALUE
    timeout > Int.MAX_VALUE -> Int.MAX_VALUE
    else -> timeout.toInt()
}

@InternalAPI
fun convertLongTimeoutToLongWithInfiniteAsZero(timeout: Long): Long = when (timeout) {
    HttpTimeout.INFINITE_TIMEOUT_MS -> 0L
    else -> timeout
}
