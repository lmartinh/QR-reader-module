package com.lmartinh.qrreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lmartinh.qrreader.databinding.ActivityMainBinding
import com.lmartinh.qrreader.internal.QrReader
import com.lmartinh.qrreader.internal.QrReaderError
import com.lmartinh.qrreader.internal.QrReaderSuccess
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.plant(Timber.DebugTree())
        val qrReader = QrReader()
        qrReader.init(this)

        binding.mainButton.setOnClickListener {
            qrReader.launch(5000){ qrReaderResponse ->
                when(qrReaderResponse){
                    is QrReaderError -> {
                        Timber.d("QR READER ERROR: ${qrReaderResponse.exception}")
                        Toast.makeText(this,"QR READER ERROR: ${qrReaderResponse.exception}", Toast.LENGTH_LONG).show()
                    }
                    is QrReaderSuccess -> {
                        Timber.d("QR READER: ${qrReaderResponse.data}")
                        Toast.makeText(this,"QR READER: ${qrReaderResponse.data}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}