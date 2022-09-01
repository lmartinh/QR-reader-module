package com.lmartinh.qrreader.internal.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lmartinh.qrreader.databinding.FragmentQrReaderBinding
import com.lmartinh.qrreader.internal.QrReaderCameraManager
import com.lmartinh.qrreader.internal.QrReaderError
import com.lmartinh.qrreader.internal.QrReaderResponse
import com.lmartinh.qrreader.internal.QrReaderSuccess
import com.lmartinh.qrreader.internal.observeOnce
import timber.log.Timber


internal class QrReaderFragment : Fragment() {

    private var _binding: FragmentQrReaderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QrReaderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQrReaderBinding.inflate(inflater, container, false)

        val cameraManager = QrReaderCameraManager(
            owner = requireActivity(),
            context = requireContext(),
            viewFinder = binding.qrReaderCameraView,
        )
        viewModel.setCameraManager(cameraManager)

        viewModel.startCamera(
            5000
        )
        viewModel.qrResult.observeOnce(this) { qrData ->
            Timber.d("QR CODE: $qrData")
            returnResponse(QrReaderSuccess(qrData))
        }

        viewModel.error.observeOnce(this){ exception ->
            returnResponse(QrReaderError(exception))
        }

        return binding.root
    }

    private fun returnResponse(response: QrReaderResponse){

        val output = Intent()
        output.putExtra(
            "result", response
        )
        requireActivity().setResult(AppCompatActivity.RESULT_OK, output)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        viewModel.stopCamera()
        _binding = null
        super.onDestroyView()
    }

}