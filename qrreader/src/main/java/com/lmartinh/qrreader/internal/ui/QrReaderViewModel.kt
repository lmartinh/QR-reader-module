package com.lmartinh.qrreader.internal.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lmartinh.qrreader.internal.QrReaderCameraManager
import com.lmartinh.qrreader.internal.model.QrException

internal class QrReaderViewModel : ViewModel() {

    private lateinit var cameraManager: QrReaderCameraManager
    private var timeoutHandler = Handler(Looper.getMainLooper())

    private val _qrResult: MutableLiveData<String> = MutableLiveData()
    val qrResult: LiveData<String> = _qrResult

    private val _error: MutableLiveData<QrException> = MutableLiveData()
    val error: LiveData<QrException> = _error

    private val _cameraReady: MutableLiveData<Unit> = MutableLiveData()
    val cameraReady: LiveData<Unit> = _cameraReady

    fun setCameraManager(cameraManager: QrReaderCameraManager) {
        this.cameraManager = cameraManager
    }

    fun startCamera(timeout: Long) {
        try {
            cameraManager.cameraReady = _cameraReady
            cameraManager.start(onQrReaderResult = ::onQrReaderResult)
            startTimeout(timeout)
        } catch (exception: Exception) {
            _error.postValue(QrException.CAMERA_MANAGER_ERROR)
        }
    }

    private fun startTimeout(timeout: Long) {
        timeoutHandler.postDelayed(
            {
                _error.postValue(QrException.TIMEOUT)
            },
            timeout
        )
    }

    private fun onQrReaderResult(result: String) {
        _qrResult.postValue(result)
        stopTimeout()
        stopCamera()
    }

    fun stopCamera() {
        cameraManager.stop()
    }

    fun cameraHasFlash(): Boolean = cameraManager.hasFlash()

    fun turnOnFlashLight(state: Boolean) = cameraManager.turnOnFlashLight(state)

    private fun stopTimeout() {
        timeoutHandler.removeCallbacksAndMessages(null)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimeout()
    }
}
