/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.cio.backend

import io.ktor.util.*

/**
 * HTTP server connector settings
 * @property host to listen to
 * @property port to listen to
 * @property connectionIdleTimeoutSeconds time to live for IDLE connections
 */
@KtorExperimentalAPI
data class HttpServerSettings(
    val host: String = "0.0.0.0",
    val port: Int = 8080,
    val connectionIdleTimeoutSeconds: Long = 45
)
