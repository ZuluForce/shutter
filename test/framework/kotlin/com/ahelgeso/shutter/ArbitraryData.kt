package com.ahelgeso.shutter

import java.util.concurrent.atomic.AtomicLong

object ArbitraryData {
    val counter: AtomicLong = AtomicLong()

    fun arbitraryLong() = counter.getAndIncrement()
    fun arbitraryInt() = counter.getAndIncrement().toInt()
    fun arbitraryString() = "Test String: ${arbitraryLong()}"
}