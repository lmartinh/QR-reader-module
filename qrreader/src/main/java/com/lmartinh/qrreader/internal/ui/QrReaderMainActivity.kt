package com.lmartinh.qrreader.internal.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lmartinh.qrreader.R
import com.lmartinh.qrreader.internal.QrException
import com.lmartinh.qrreader.internal.QrReaderError
import com.lmartinh.qrreader.internal.utils.Constants
import timber.log.Timber

internal class QrReaderMainActivity : AppCompatActivity() {

    private val permissionArray = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_reader_main)

        Timber.plant(Timber.DebugTree())
        requestPermissions()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.qr_reader_nav_host_fragment) as NavHostFragment

        navHostFragment.navController.setGraph(R.navigation.qr_reader_navigation, intent?.extras)
    }

    private fun requestPermissions() {
        val requestMultiplePermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.map {
                it.value
            }

            if (!granted.all { it }) {

                MaterialAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setMessage(resources.getString(R.string.qr_reader_text_permission_denied))
                    .setPositiveButton(resources.getString(R.string.qr_reader_text_exit)) { dialog, _ ->
                        dialog.dismiss()
                        val output = Intent()
                        output.putExtra(
                            Constants.RESULT_EXTRA,
                            QrReaderError(QrException.PERMISSION_NOT_GRANTED)
                        )
                        setResult(RESULT_OK, output)
                        this.finish()
                    }
                    .show()
            }
        }

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            requestMultiplePermissions.launch(
                permissionArray
            )
        }
    }

    override fun onBackPressed() {
        val output = Intent()
        output.putExtra(
            Constants.RESULT_EXTRA,
            QrReaderError(QrException.CANCEL_BY_USER)
        )
        setResult(RESULT_OK, output)
        this.finish()
    }

}