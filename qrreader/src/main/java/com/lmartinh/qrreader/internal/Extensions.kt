package com.lmartinh.qrreader.internal

import java.nio.ByteBuffer

internal fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}
