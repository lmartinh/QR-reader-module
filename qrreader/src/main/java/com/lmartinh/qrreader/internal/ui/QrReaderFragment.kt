package com.lmartinh.qrreader.internal.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.lmartinh.qrreader.R
import com.lmartinh.qrreader.databinding.FragmentQrReaderBinding
import com.lmartinh.qrreader.internal.QrReaderCameraManager
import com.lmartinh.qrreader.internal.QrReaderError
import com.lmartinh.qrreader.internal.QrReaderResponse
import com.lmartinh.qrreader.internal.QrReaderSuccess
import com.lmartinh.qrreader.internal.observeOnce
import com.lmartinh.qrreader.internal.utils.Constants
import timber.log.Timber


internal class QrReaderFragment : Fragment() {

    private var _binding: FragmentQrReaderBinding? = null
    private val binding get() = _binding!!
    private val arguments: QrReaderFragmentArgs by navArgs()
    private val viewModel: QrReaderViewModel by viewModels()
    private var flashOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrReaderBinding.inflate(inflater, container, false)

        val cameraManager = QrReaderCameraManager(
            owner = requireActivity(),
            context = requireContext(),
            viewFinder = binding.qrReaderCameraView,
        )
        viewModel.setCameraManager(cameraManager)

        viewModel.startCamera(
            arguments.timeout
        )
        viewModel.qrResult.observeOnce(this) { qrData ->
            Timber.d("QR CODE: $qrData")
            returnResponse(QrReaderSuccess(qrData))
        }

        viewModel.error.observeOnce(this){ exception ->
            returnResponse(QrReaderError(exception))
        }

        binding.qrReaderCameraLayout.qrReaderLayoutClose.setOnClickListener{
            activity?.onBackPressed()
        }

        viewModel.cameraReady.observeOnce(this){
            binding.qrReaderCameraLayout.qrReaderLayoutFlash.visibility = if (viewModel.cameraHasFlash()){
                View.VISIBLE
            }else{
                View.GONE
            }
        }

        binding.qrReaderCameraLayout.qrReaderLayoutFlash.setOnClickListener {
            flashOn = !flashOn
            viewModel.turnOnFlashLight(flashOn)
            switchFlashImage()
        }

        return binding.root
    }

    private fun switchFlashImage(){
        binding.qrReaderCameraLayout.qrReaderLayoutFlash.setImageResource(
            if (flashOn){
                R.drawable.ic_qr_reader_flash_on
            }else{
                R.drawable.ic_qr_reader_flash_off
            }

        )

    }

    private fun returnResponse(response: QrReaderResponse){

        val output = Intent()
        output.putExtra(
            Constants.RESULT_EXTRA, response
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