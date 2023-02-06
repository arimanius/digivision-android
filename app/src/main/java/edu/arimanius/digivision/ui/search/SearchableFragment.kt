package edu.arimanius.digivision.ui.search

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import edu.arimanius.digivision.MainActivity
import java.io.File

abstract class SearchableFragment : Fragment() {
    private val PERMISSION_REQUEST_CODE = 200
    protected lateinit var viewModel: SearchViewModel
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    protected abstract val searchButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(requireContext())
        )[SearchViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                onImageCropped(result.uriContent!!)
            }
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    startCropper(uri)
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        val file = File(requireContext().cacheDir, "photo.png")
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            file
        )

        val takePicture =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    Log.d("Camera", "Picture taken")
                    startCropper(uri)
                } else {
                    Log.d("Camera", "Picture not taken")
                }
            }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("از کدام روش؟")
            .setCancelable(true)
            .setPositiveButton("دوربین") { _, _ ->
                takePicture.launch(uri)
            }
            .setNegativeButton("گالری") { _, _ ->
                pickMedia.launch(PickVisualMediaRequest.Builder().build())
            }

        searchButton.setOnClickListener {
            if (!checkPermission()) {
                requestPermission()
            }
            dialogBuilder.show()
        }
    }

    protected abstract fun onImageCropped(uri: Uri)

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun startCropper(uri: Uri) {
        val byteArray = loadImageToByteArray(uri)
        (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.VISIBLE
        val cropResult = viewModel.crop(byteArray)
        cropResult.observe(viewLifecycleOwner) { bb ->
            val cropRect = Rect(
                bb.topLeft.x - ((bb.bottomRight.x - bb.topLeft.x).toFloat() / 10).toInt(),
                bb.topLeft.y - ((bb.bottomRight.y - bb.topLeft.y).toFloat() / 10).toInt(),
                bb.bottomRight.x + ((bb.bottomRight.x - bb.topLeft.x).toFloat() / 10).toInt(),
                bb.bottomRight.y + ((bb.bottomRight.y - bb.topLeft.y).toFloat() / 10).toInt(),
            )
            Log.d("CROP", bb.toString())
            (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.GONE
            cropImage.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        initialCropWindowRectangle = cropRect,
                    ),
                ),
            )
        }
    }

    protected fun loadImageToByteArray(uri: Uri): ByteArray {
        requireContext().contentResolver.openInputStream(uri).use { inputStream ->
            return inputStream!!.readBytes()
        }
    }
}