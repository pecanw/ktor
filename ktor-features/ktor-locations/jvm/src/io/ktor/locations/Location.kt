/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("unused")
@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.ktor.locations

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlin.reflect.*

/**
 * API marked with this annotation is experimental and is not guaranteed to be stable.
 */
@Suppress("DEPRECATION")
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This locations API is experimental. It could be changed or removed in future releases."
)
@Experimental(level = Experimental.Level.WARNING)
annotation class KtorExperimentalLocationsAPI

/**
 * Annotation for classes that will act as typed routes.
 * @property path the route path, including class property names wrapped with curly braces.
 */
@KtorExperimentalLocationsAPI
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
annotation class Location(val path: String)

/**
 * Gets the [Application.locations] feature
 */
@KtorExperimentalLocationsAPI
val PipelineContext<Unit, ApplicationCall>.locations: Locations
    get() = call.application.locations

/**
 * Gets the [Application.locations] feature
 */
@KtorExperimentalLocationsAPI
val ApplicationCall.locations: Locations
    get() = application.locations

/**
 * Gets the [Application.locations] feature
 */
@KtorExperimentalLocationsAPI
val Application.locations: Locations
    get() = feature(Locations)

/**
 * Renders link to a [location] using current installed locations service
 * @throws MissingApplicationFeatureException is no locations feature installed
 */
@KtorExperimentalLocationsAPI
fun PipelineContext<Unit, ApplicationCall>.href(location: Any): String {
    return call.application.locations.href(location)
}

/**
 * Registers a route [body] for a location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.location(noinline body: Route.() -> Unit): Route {
    return location(T::class, body)
}

/**
 * Registers a typed handler [body] for a `GET` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.get(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Get) {
            handle(body)
        }
    }
}

/**
 * Registers a typed handler [body] for a `OPTIONS` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.options(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Options) {
            handle(body)
        }
    }
}

/**
 * Registers a typed handler [body] for a `HEAD` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.head(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Head) {
            handle(body)
        }
    }
}

/**
 * Registers a typed handler [body] for a `POST` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.post(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Post) {
            handle(body)
        }
    }
}

/**
 * Registers a typed handler [body] for a `PUT` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.put(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Put) {
            handle(body)
        }
    }
}

/**
 * Registers a typed handler [body] for a `DELETE` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.delete(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Delete) {
            handle(body)
        }
    }
}

/**
 * Registers a typed handler [body] for a `PATCH` location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [T] as first parameter.
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.patch(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit): Route {
    return location(T::class) {
        method(HttpMethod.Patch) {
            handle(body)
        }
    }
}

/**
 * Registers a route [body] for a location defined by class [data].
 *
 * Class [data] **must** be annotated with [Location].
 */
@KtorExperimentalLocationsAPI
fun <T : Any> Route.location(data: KClass<T>, body: Route.() -> Unit): Route {
    val entry = application.locations.createEntry(this, data)
    return entry.apply(body)
}

/**
 * Registers a handler [body] for a location defined by class [T].
 *
 * Class [T] **must** be annotated with [Location].
 */
@KtorExperimentalLocationsAPI
inline fun <reified T : Any> Route.handle(noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit) {
    return handle(T::class, body)
}

/**
 * Registers a handler [body] for a location defined by class [dataClass].
 *
 * Class [dataClass] **must** be annotated with [Location].
 *
 * @param body receives an instance of typed location [dataClass] as first parameter.
 */
@KtorExperimentalLocationsAPI
fun <T : Any> Route.handle(dataClass: KClass<T>, body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit) {
    intercept(ApplicationCallPipeline.Features) {
        call.attributes.put(LocationInstanceKey, locations.resolve<T>(dataClass, call))
    }

    handle {
        @Suppress("UNCHECKED_CAST")
        val location = call.attributes[LocationInstanceKey] as T

        body(location)
    }
}

/**
 * Retrieves the current call's location or `null` if it is not available (request is not handled by a location class),
 * or not yet available (invoked too early before the locations feature takes place).
 */
@KtorExperimentalAPI
inline fun <reified T : Any> ApplicationCall.locationOrNull(): T = locationOrNull(T::class)

@PublishedApi
internal fun <T : Any> ApplicationCall.locationOrNull(type: KClass<T>): T =
    attributes.getOrNull(LocationInstanceKey)?.let { instance ->
        type.cast(instance)
    } ?: error("Location instance is not available for this call.)")

private val LocationInstanceKey = AttributeKey<Any>("LocationInstance")

private fun <T : Any> KClass<T>.cast(instance: Any): T {
    return javaObjectType.cast(instance)
}
