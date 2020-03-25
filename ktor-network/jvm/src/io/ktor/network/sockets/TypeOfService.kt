/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

/**
 * An inline class to hold a IP ToS value
 * @property value an unsigned byte IP_TOS value
 */
@OptIn(ExperimentalUnsignedTypes::class)
inline class TypeOfService(val value: UByte) {
    /**
     * Creates ToS by integer value discarding extra high bits
     */
    constructor(value: Int) : this(value.toUByte())

    /**
     * Integer representation of this ToS
     */
    inline val intValue: Int get() = value.toInt()

    @Suppress("KDocMissingDocumentation")
    companion object {
        val UNDEFINED: TypeOfService = TypeOfService(0u)
        val IPTOS_LOWCOST: TypeOfService = TypeOfService(0x02u)
        val IPTOS_RELIABILITY: TypeOfService = TypeOfService(0x04u)
        val IPTOS_THROUGHPUT: TypeOfService = TypeOfService(0x08u)
        val IPTOS_LOWDELAY: TypeOfService = TypeOfService(0x10u)
    }
}
