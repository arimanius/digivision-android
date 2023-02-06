package edu.arimanius.digivision.ui.search

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

abstract class SearchableFragment : Fragment() {
    private val PERMISSION_REQUEST_CODE = 200
    protected lateinit var viewModel: SearchViewModel
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    protected abstract val searchButton: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(requireContext())
        )[SearchViewModel::class.java]

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
        val resized = resizeTheImage(byteArray, 512)
        val cropResult = viewModel.crop(resized.first)
        cropResult.observe(viewLifecycleOwner) { bb ->
            val cropRect = Rect(
                (bb.topLeft.x / resized.second).toInt(),
                (bb.topLeft.y / resized.second).toInt(),
                (bb.bottomRight.x / resized.second).toInt(),
                (bb.bottomRight.y / resized.second).toInt()
            )
            cropRect.left = max(
                0,
                cropRect.left - ((cropRect.right - cropRect.left).toFloat() / 10).toInt()
            )
            cropRect.top = max(
                0,
                cropRect.top - ((cropRect.bottom - cropRect.top).toFloat() / 10).toInt()
            )
            cropRect.right += ((cropRect.right - cropRect.left).toFloat() / 10).toInt()
            cropRect.bottom += ((cropRect.bottom - cropRect.top).toFloat() / 10).toInt()
            Log.d("CROP", bb.toString())
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

    protected fun resizeTheImage(
        bytes: ByteArray?,
        w: Int = -1,
        h: Int = -1
    ): Pair<ByteArray, Float> {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
        Log.d("PhotoPicker", "Original size: ${bitmap.width}x${bitmap.height}")
        var width = w
        var height = h
        var scale = width.toFloat() / bitmap.width.toFloat()
        if (width == -1) {
            assert(height != -1)
            scale = height.toFloat() / bitmap.height.toFloat()
            val ratio = bitmap.height.toFloat() / h.toFloat()
            width = (bitmap.width / ratio).toInt()
        } else if (h == -1) {
            assert(width != -1)
            val ratio = w.toFloat() / bitmap.width.toFloat()
            height = (bitmap.height * ratio).toInt()
        }
        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)
        Log.d("PhotoPicker", "Resized size: ${resized.width}x${resized.height}")
        Log.d("PhotoPicker", "Scale: $scale")
        return Pair(bitmapToByteArray(resized), scale)
    }

    protected fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}