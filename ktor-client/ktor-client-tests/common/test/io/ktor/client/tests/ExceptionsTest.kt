/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import kotlin.test.*


class ExceptionsTest {
    @Test
    fun testReadResponseFromException() = testSuspend {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respondError(HttpStatusCode.BadRequest)
                }
            }
        }

        try {
            client.get<String>("www.google.com")
        } catch (exception: ResponseException) {
            val text = exception.response.readText()
            assertEquals(HttpStatusCode.BadRequest.description, text)
        }
    }
}
