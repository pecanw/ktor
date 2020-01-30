/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.cio.backend

import io.ktor.http.cio.*
import io.ktor.http.cio.internals.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.engine.*
import io.ktor.util.*
import kotlinx.coroutines.*
import org.slf4j.*
import java.nio.channels.*

/**
 * Represents a server instance
 * @property rootServerJob server job - root for all jobs
 * @property acceptJob client connections accepting job
 * @property serverSocket a deferred server socket instance, could be completed with error if it failed to bind
 */
@Suppress("MemberVisibilityCanBePrivate")
@KtorExperimentalAPI
class HttpServer(val rootServerJob: Job, val acceptJob: Job, val serverSocket: Deferred<ServerSocket>)

/**
 * Start an http server with [settings] invoking [handler] for every request
 */
@UseExperimental(InternalAPI::class)
fun CoroutineScope.httpServer(
    settings: HttpServerSettings,
    handler: HttpRequestHandler
): HttpServer {
    val socket = CompletableDeferred<ServerSocket>()

    val serverLatch: CompletableJob = Job()
    val serverJob = launch(
        context = CoroutineName("server-root-${settings.port}"),
        start = CoroutineStart.UNDISPATCHED
    ) {
        serverLatch.join()
    }

    val selector = ActorSelectorManager(coroutineContext)
    val timeout = WeakTimeoutQueue(
        settings.connectionIdleTimeoutSeconds * 1000L
    )

    val logger = LoggerFactory.getLogger(HttpServer::class.java)

    val acceptJob = launch(serverJob + CoroutineName("accept-${settings.port}")) {
        aSocket(selector).tcp().bind(settings.host, settings.port).use { server ->
            socket.complete(server)

            val connectionScope = CoroutineScope(
                coroutineContext +
                    SupervisorJob(serverJob) +
                    DefaultUncaughtExceptionHandler(logger) +
                    CoroutineName("request")
            )

            try {
                while (true) {
                    val client: Socket = server.accept()

                    val connection = ServerIncomingConnection(
                        client.openReadChannel(),
                        client.openWriteChannel(),
                        client.remoteAddress
                    )
                    val clientJob = connectionScope.startServerConnectionPipeline(
                        connection,
                        timeout,
                        handler
                    )

                    clientJob.invokeOnCompletion {
                        client.close()
                    }
                }
            } catch (closed: ClosedChannelException) {
                coroutineContext.cancel()
            } finally {
                server.close()
                server.awaitClosed()
                connectionScope.coroutineContext.cancel()
            }
        }
    }

    acceptJob.invokeOnCompletion { cause ->
        cause?.let { socket.completeExceptionally(it) }
        serverLatch.complete()
        timeout.process()
    }

    @UseExperimental(InternalCoroutinesApi::class) // TODO it's attach child?
    serverJob.invokeOnCompletion(onCancelling = true) {
        timeout.cancel()
    }
    serverJob.invokeOnCompletion {
        selector.close()
    }

    return HttpServer(serverJob, acceptJob, socket)
}
