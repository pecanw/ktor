/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.locations

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlin.reflect.*

/**
 * Ktor feature that allows to handle and construct routes in a typed way.
 *
 * You have to create data classes/objects representing parameterized routes and annotate them with [Location].
 * Then you can register sub-routes and handlers for those locations and create links to them
 * using [Locations.href].
 */
open class Locations @KtorExperimentalLocationsAPI constructor(
    application: Application,
    routeService: LocationRouteService
) {
    /**
     * Creates Locations service extracting path information from @Location annotation
     */
    @OptIn(KtorExperimentalLocationsAPI::class)
    constructor(application: Application) : this(application, LocationAttributeRouteService())

    private val implementation: LocationsImpl = BackwardCompatibleImpl(application, routeService)

    /**
     * All locations registered at the moment (Immutable list).
     */
    @KtorExperimentalLocationsAPI
    val registeredLocations: List<LocationInfo>
        get() = implementation.registeredLocations

    /**
     * Resolves parameters in a [call] to an instance of specified [locationClass].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(locationClass: KClass<*>, call: ApplicationCall): T {
        return resolve(locationClass, call.parameters)
    }

    /**
     * Resolves [parameters] to an instance of specified [locationClass].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(locationClass: KClass<*>, parameters: Parameters): T {
        val info = implementation.getOrCreateInfo(locationClass)
        return implementation.instantiate(info, parameters) as T
    }

    /**
     * Resolves [parameters] to an instance of specified [T].
     */
    @KtorExperimentalLocationsAPI
    inline fun <reified T : Any> resolve(parameters: Parameters): T {
        return resolve(T::class, parameters) as T
    }

    /**
     * Resolves parameters in a [call] to an instance of specified [T].
     */
    @KtorExperimentalLocationsAPI
    inline fun <reified T : Any> resolve(call: ApplicationCall): T {
        return resolve(T::class, call)
    }

    /**
     * Constructs the url for [location].
     *
     * The class of [location] instance **must** be annotated with [Location].
     */
    fun href(location: Any): String = implementation.href(location)

    internal fun href(location: Any, builder: URLBuilder) {
        implementation.href(location, builder)
    }

    @OptIn(KtorExperimentalLocationsAPI::class)
    private fun createEntry(parent: Route, info: LocationInfo): Route {
        val hierarchyEntry = info.parent?.let { createEntry(parent, it) } ?: parent
        return hierarchyEntry.createRouteFromPath(info.path)
    }

    /**
     * Creates all necessary routing entries to match specified [locationClass].
     */
    fun createEntry(parent: Route, locationClass: KClass<*>): Route {
        val info = implementation.getOrCreateInfo(locationClass)
        val pathRoute = createEntry(parent, info)

        @OptIn(KtorExperimentalLocationsAPI::class)
        return info.queryParameters.fold(pathRoute) { entry, query ->
            val selector = if (query.isOptional)
                OptionalParameterRouteSelector(query.name)
            else
                ParameterRouteSelector(query.name)
            entry.createChild(selector)
        }
    }

    /**
     * Configuration for [Locations].
     */
    class Configuration {
        /**
         * Specifies an alternative routing service. Default is [LocationAttributeRouteService].
         */
        @KtorExperimentalLocationsAPI
        var routeService: LocationRouteService? = null
    }

    /**
     * Installable feature for [Locations].
     */
    companion object Feature : ApplicationFeature<Application, Configuration, Locations> {
        override val key: AttributeKey<Locations> = AttributeKey("Locations")

        @OptIn(KtorExperimentalLocationsAPI::class)
        override fun install(pipeline: Application, configure: Configuration.() -> Unit): Locations {
            val configuration = Configuration().apply(configure)
            val routeService = configuration.routeService ?: LocationAttributeRouteService()
            return Locations(pipeline, routeService)
        }
    }
}

/**
 * Provides services for extracting routing information from a location class.
 */
@KtorExperimentalLocationsAPI
interface LocationRouteService {
    /**
     * Retrieves routing information from a given [locationClass].
     * @return routing pattern, or null if a given class doesn't represent a route.
     */
    fun findRoute(locationClass: KClass<*>): String?
}

/**
 * Implements [LocationRouteService] by extracting routing information from a [Location] annotation.
 */
@KtorExperimentalLocationsAPI
class LocationAttributeRouteService : LocationRouteService {
    private inline fun <reified T : Annotation> KAnnotatedElement.annotation(): T? {
        return annotations.singleOrNull { it.annotationClass == T::class } as T?
    }

    override fun findRoute(locationClass: KClass<*>): String? = locationClass.annotation<Location>()?.path
}

/**
 * Exception indicating that route parameters in curly brackets do not match class properties.
 */
@KtorExperimentalLocationsAPI
class LocationRoutingException(message: String) : Exception(message)

@KtorExperimentalLocationsAPI
internal class LocationPropertyInfoImpl(
    name: String,
    val kGetter: KProperty1.Getter<Any, Any?>,
    isOptional: Boolean
) : LocationPropertyInfo(name, isOptional)
