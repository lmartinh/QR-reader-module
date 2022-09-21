package com.lmartinh.qrreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lmartinh.qrreader.databinding.ActivityMainBinding
import com.lmartinh.qrreader.internal.QrReader
import com.lmartinh.qrreader.internal.QrReaderError
import com.lmartinh.qrreader.internal.QrReaderSuccess
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        const val TIMEOUT = 15000L
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.plant(Timber.DebugTree())
        val qrReader = QrReader()
        qrReader.init(this)

        binding.mainButton.setOnClickListener {
            qrReader.launch(TIMEOUT) { qrReaderResponse ->
                when (qrReaderResponse) {
                    is QrReaderError -> {
                        Timber.d("QR READER ERROR: ${qrReaderResponse.exception}")
                        Toast.makeText(this, "QR READER ERROR: ${qrReaderResponse.exception}", Toast.LENGTH_LONG).show()
                    }
                    is QrReaderSuccess -> {
                        Timber.d("QR READER: ${qrReaderResponse.data}")
                        showAlert(qrReaderResponse.data)
                    }
                    else -> {
                        Timber.d("QR READER RESPONSE: $qrReaderResponse")
                    }
                }
            }
        }
    }

    private fun showAlert(message: String) {
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton(resources.getString(R.string.qr_reader_text_open)) { dialog, _ ->
                dialog.dismiss()
                if (URLUtil.isValidUrl(message)) {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(message))
                    startActivity(browserIntent)
                } else {
                    Toast.makeText(this, "URL ERROR", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(resources.getString(R.string.qr_reader_text_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
