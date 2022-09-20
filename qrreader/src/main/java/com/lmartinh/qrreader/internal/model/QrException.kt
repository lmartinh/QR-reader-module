package com.lmartinh.qrreader.internal.model

enum class QrException {
    TIMEOUT,
    PERMISSION_NOT_GRANTED,
    CAMERA_MANAGER_ERROR,
    CONTEXT_ERROR,
    CANCEL_BY_USER,
}