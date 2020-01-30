/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.cio

import io.ktor.http.cio.*
import io.ktor.server.cio.backend.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

/**
 * Represents a server instance
 * @property rootServerJob server job - root for all jobs
 * @property acceptJob client connections accepting job
 * @property serverSocket a deferred server socket instance, could be completed with error if it failed to bind
 */
@Deprecated("Use io.ktor.server.cio.backend package instead.")
typealias HttpServer = io.ktor.server.cio.backend.HttpServer

/**
 * HTTP server connector settings
 * @property host to listen to
 * @property port to listen to
 * @property connectionIdleTimeoutSeconds time to live for IDLE connections
 */
@Deprecated("Use io.ktor.server.cio.backend package instead.")
typealias HttpServerSettings = io.ktor.server.cio.backend.HttpServerSettings

/**
 * Start an http server with [settings] invoking [handler] for every request
 */
@Deprecated("Use handler function with single request parameter from package io.ktor.server.cio.backend.")
fun CoroutineScope.httpServer(
    settings: HttpServerSettings,
    handler: suspend CoroutineScope.(
        request: Request,
        input: ByteReadChannel, output: ByteWriteChannel, upgraded: CompletableDeferred<Boolean>?
    ) -> Unit
): HttpServer {
    return httpServer(settings) { request ->
        handler(this, request, input, output, upgraded)
    }
}
