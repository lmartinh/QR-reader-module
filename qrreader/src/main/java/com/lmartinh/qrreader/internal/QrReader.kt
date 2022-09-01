package com.lmartinh.qrreader.internal

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.lmartinh.qrreader.internal.ui.QrReaderMainActivity

class QrReader {

    companion object{
        const val TIMEOUT_15000 = 15000L
        const val TIMEOUT_EXTRA = "timeout"
    }

    var output: ((QrReaderResponse) -> Unit)? = null
    private lateinit var register: ActivityResultLauncher<Intent>
    private var context: Context? = null

    private val contract: ActivityResultContracts.StartActivityForResult =
        ActivityResultContracts.StartActivityForResult()

    fun init(activity: ComponentActivity) {
        this.context = activity

        register = activity.registerForActivityResult(contract) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK || activityResult.resultCode == Activity.RESULT_CANCELED) {
                if (activityResult.data != null) {
                    val qrResult = activityResult.data.let {
                        it?.getParcelableExtra<QrReaderResponse>("result")
                    }
                    if (qrResult != null) {
                        output?.invoke(qrResult)

                    } else {
                        output?.invoke(QrReaderError(QrException.CAMERA_MANAGER_ERROR))
                    }

                }
            }
        }
    }

    fun launch(timeout: Long = TIMEOUT_15000, output: ((QrReaderResponse) -> Unit)) {
        this.output = output
        if (context != null) {
            val intent = Intent(context, QrReaderMainActivity::class.java).apply {
                putExtra(TIMEOUT_EXTRA, timeout)
            }
            register.launch(intent)
        } else {
            output(QrReaderError(QrException.CONTEXT_ERROR))
        }
    }
}