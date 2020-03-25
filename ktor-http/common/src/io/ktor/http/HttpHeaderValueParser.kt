/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.http

import io.ktor.util.*

/**
 * Represents a single value parameter
 * @property name of parameter
 * @property value of parameter
 */
data class HeaderValueParam(val name: String, val value: String) {
    override fun equals(other: Any?): Boolean {
        return other is HeaderValueParam
            && other.name.equals(name, ignoreCase = true)
            && other.value.equals(value, ignoreCase = true)
    }

    override fun hashCode(): Int {
        var result = name.toLowerCase().hashCode()
        result += 31 * result + value.toLowerCase().hashCode()
        return result
    }
}

/**
 * Represents a header value. Similar to [HeaderValueWithParameters]
 * @property value
 * @property params for this value (could be empty)
 */
data class HeaderValue(val value: String, val params: List<HeaderValueParam> = listOf()) {
    /**
     * Value's quality according to `q` parameter or `1.0` if missing or invalid
     */
    val quality: Double =
        params.firstOrNull { it.name == "q" }?.value?.toDoubleOrNull()?.takeIf { it in 0.0..1.0 } ?: 1.0
}

/**
 * Parse header value and sort multiple values according to qualities
 */
fun parseAndSortHeader(header: String?): List<HeaderValue> = parseHeaderValue(header).sortedByDescending { it.quality }

/**
 * Parse `Content-Type` header values and sort them by quality and asterisks quantity
 */
fun parseAndSortContentTypeHeader(header: String?): List<HeaderValue> = parseHeaderValue(header).sortedWith(
    compareByDescending<HeaderValue> { it.quality }.thenBy {
        val contentType = ContentType.parse(it.value)
        var asterisks = 0
        if (contentType.contentType == "*")
            asterisks += 2
        if (contentType.contentSubtype == "*")
            asterisks++
        asterisks
    }.thenByDescending { it.params.size })

/**
 * Parse header value respecting multi-values
 */
fun parseHeaderValue(text: String?): List<HeaderValue> {
    return parseHeaderValue(text, false)
}

/**
 * Parse header value respecting multi-values
 * @param parametersOnly if no header value itself, only parameters
 */
fun parseHeaderValue(text: String?, parametersOnly: Boolean): List<HeaderValue> {
    if (text == null)
        return emptyList()

    var pos = 0
    val items = lazy(LazyThreadSafetyMode.NONE) { arrayListOf<HeaderValue>() }
    while (pos <= text.lastIndex) {
        pos = parseHeaderValueItem(text, pos, items, parametersOnly)
    }
    return items.valueOrEmpty()
}

/**
 * Construct a list of [HeaderValueParam] from an iterable of pairs
 */
@KtorExperimentalAPI
fun Iterable<Pair<String, String>>.toHeaderParamsList(): List<HeaderValueParam> =
    map { HeaderValueParam(it.first, it.second) }

private fun <T> Lazy<List<T>>.valueOrEmpty(): List<T> = if (isInitialized()) value else emptyList()
private fun String.subtrim(start: Int, end: Int): String {
    return substring(start, end).trim()
}

private fun parseHeaderValueItem(
    text: String,
    start: Int,
    items: Lazy<ArrayList<HeaderValue>>,
    parametersOnly: Boolean
): Int {
    var pos = start
    val parameters = lazy(LazyThreadSafetyMode.NONE) { arrayListOf<HeaderValueParam>() }
    var valueEnd: Int? = if (parametersOnly) pos else null
    while (pos <= text.lastIndex) {
        when (text[pos]) {
            ',' -> {
                items.value.add(HeaderValue(text.subtrim(start, valueEnd ?: pos), parameters.valueOrEmpty()))
                return pos + 1
            }
            ';' -> {
                if (valueEnd == null) valueEnd = pos
                pos = parseHeaderValueParameter(text, pos + 1, parameters)
            }
            else -> {
                pos = if (parametersOnly) {
                    parseHeaderValueParameter(text, pos, parameters)
                } else {
                    pos + 1
                }
            }
        }
    }
    items.value.add(HeaderValue(text.subtrim(start, valueEnd ?: pos), parameters.valueOrEmpty()))
    return pos
}

private fun parseHeaderValueParameter(text: String, start: Int, parameters: Lazy<ArrayList<HeaderValueParam>>): Int {
    fun addParam(text: String, start: Int, end: Int, value: String) {
        val name = text.subtrim(start, end)
        if (name.isEmpty())
            return
        parameters.value.add(HeaderValueParam(name, value))
    }

    var pos = start
    while (pos <= text.lastIndex) {
        when (text[pos]) {
            '=' -> {
                val (paramEnd, paramValue) = parseHeaderValueParameterValue(text, pos + 1)
                addParam(text, start, pos, paramValue)
                return paramEnd
            }
            ';', ',' -> {
                addParam(text, start, pos, "")
                return pos
            }
            else -> pos++
        }
    }

    addParam(text, start, pos, "")
    return pos
}


private fun parseHeaderValueParameterValue(value: String, start: Int): Pair<Int, String> {
    var pos = start
    while (pos <= value.lastIndex) {
        when (value[pos]) {
            '"' -> return parseHeaderValueParameterValueQuoted(value, pos + 1)
            ';', ',' -> return pos to value.subtrim(start, pos)
            else -> pos++
        }
    }
    return pos to value.subtrim(start, pos)
}

private fun parseHeaderValueParameterValueQuoted(value: String, start: Int): Pair<Int, String> {
    var pos = start
    val sb = StringBuilder()
    while (pos <= value.lastIndex) {
        val c = value[pos]
        when (c) {
            '"' -> return pos + 1 to sb.toString()
            '\\' -> {
                if (pos < value.lastIndex - 2) {
                    sb.append(value[pos + 1])
                    pos += 2
                } // quoted value
                else {
                    sb.append(c)
                    pos++ // broken value, escape at the end
                }
            }
            else -> {
                sb.append(c)
                pos++
            }
        }
    }
    return pos to sb.toString()
}
