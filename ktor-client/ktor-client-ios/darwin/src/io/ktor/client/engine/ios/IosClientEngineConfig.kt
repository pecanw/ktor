/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.ios

import io.ktor.client.engine.*
import platform.Foundation.*

/**
 * Challenge handler type for [NSURLSession].
 */
typealias ChallengeHandler = (
    session: NSURLSession,
    task: NSURLSessionTask,
    challenge: NSURLAuthenticationChallenge,
    completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
) -> Unit

/**
 * Custom [IosClientEngine] config.
 */
class IosClientEngineConfig : HttpClientEngineConfig() {
    /**
     * Request configuration.
     */
    var requestConfig: NSMutableURLRequest.() -> Unit = {}
        @Deprecated(
            "[requestConfig] property is deprecated. Consider using [configureRequest] instead",
            replaceWith = ReplaceWith("this.configureRequest(value)")
        )
        set(value) {
            field = value
        }

    /**
     * Session configuration.
     */
    var sessionConfig: NSURLSessionConfiguration.() -> Unit = {}
        @Deprecated(
            "[sessionConfig] property is deprecated. Consider using [configureSession] instead",
            replaceWith = ReplaceWith("this.configureSession(value)")
        )
        set(value) {
            field = value
        }

    /**
     * Handles the challenge of HTTP responses [NSURLSession].
     */
    var challengeHandler: ChallengeHandler? = null
        private set

    /**
     * Appends block with [NSMutableURLRequest] configuration to [requestConfig].
     */
    fun configureRequest(block: NSMutableURLRequest.() -> Unit) {
        val old = requestConfig

        @Suppress("DEPRECATION")
        requestConfig = {
            old()
            block()
        }
    }

    /**
     * Appends block with [NSURLSessionConfiguration] configuration to [sessionConfig].
     */
    fun configureSession(block: NSURLSessionConfiguration.() -> Unit) {
        val old = sessionConfig

        @Suppress("DEPRECATION")
        sessionConfig = {
            old()
            block()
        }
    }

    /**
     * Sets the [block] as an HTTP request challenge handler replacing the old one.
     */
    fun handleChallenge(block: ChallengeHandler) {
        challengeHandler = block
    }
}
