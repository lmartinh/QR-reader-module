package com.lmartinh.qrreader.internal

import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class QrReaderCameraManager(
    private val owner: ComponentActivity,
    private val context: Context,
    private val viewFinder: PreviewView,
) {

    companion object {
        private const val ZERO_VALUE = 0
    }

    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
        )
        setHints(map)
    }

    private lateinit var onQrReaderResult: (result: String) -> Unit

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    lateinit var cameraReady: MutableLiveData<Unit>

    fun start(onQrReaderResult: (result: String) -> Unit) {
        this.onQrReaderResult = onQrReaderResult
        startCamera()
    }

    fun stop() {
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {

                cameraProvider = cameraProviderFuture.get()

                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Back and front camera are unavailable")
                }
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindCameraUseCases() {

        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val previewView = getPreviewUseCase()
        val imageRecognizer = getImageAnalyzerUseCase()

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                owner,
                cameraSelector,
                previewView,
                imageRecognizer
            )

            previewView.setSurfaceProvider(viewFinder.surfaceProvider)
            cameraReady.postValue(Unit)
        } catch (exception: Exception) {
            Timber.e("Bind camera error: $exception")
        }
    }

    private fun hasBackCamera() =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    private fun hasFrontCamera() =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    private fun getPreviewUseCase(): Preview {
        return Preview.Builder()
            .build()
    }

    private fun getImageAnalyzerUseCase(): ImageAnalysis {
        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(10)
            .build()

        analyzer.setAnalyzer(
            cameraExecutor,
            ImageScanner { image, rotation ->
                try {
                    val data = image.planes[ZERO_VALUE].buffer.toByteArray()
                    val source = PlanarYUVLuminanceSource(
                        data,
                        image.width,
                        image.height,
                        ZERO_VALUE,
                        ZERO_VALUE,
                        image.width,
                        image.height,
                        false
                    )

                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    val result = reader.decode(binaryBitmap)
                    onQrReaderResult(result.text)
                } catch (exception: Exception) {
                    if (exception is com.google.zxing.NotFoundException) {
                        Timber.e("QR NOT FOUND: $exception")
                    } else {
                        Timber.e("Set camera analyzer error: $exception")
                    }
                }
            }
        )

        return analyzer
    }

    fun turnOnFlashLight(state: Boolean) = camera?.cameraControl?.enableTorch(state)

    fun hasFlash(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit() ?: false
    }

    private class ImageScanner(val onResult: (result: Image, rotation: Int) -> Unit) :
        ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                onResult(mediaImage, imageProxy.imageInfo.rotationDegrees)
            }
            imageProxy.close()
        }
    }
}
