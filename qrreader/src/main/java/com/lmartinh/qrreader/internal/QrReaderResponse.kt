package com.lmartinh.qrreader.internal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class QrReaderResponse: Parcelable

open class QrReaderSuccess(val data: String): QrReaderResponse()
open class QrReaderError(val exception: QrException): QrReaderResponse()