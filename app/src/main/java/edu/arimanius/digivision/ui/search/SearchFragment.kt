package edu.arimanius.digivision.ui.search

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import edu.arimanius.digivision.databinding.FragmentSearchBinding
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max


/**
 * A fragment representing a list of Items.
 */
class SearchFragment : Fragment() {
    private val PERMISSION_REQUEST_CODE = 200
    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchViewModel
    private var columnCount = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Set the adapter
        with(binding.list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = SearchRecyclerViewAdapter()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory()
        )[SearchViewModel::class.java]

        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                // Use the returned uri.
                binding.imageView.setImageURI(result.uriContent)
                val resized = resizeTheImage(loadImageToByteArray(result.uriContent!!), 256, 256)
                Log.d("ImageCropper", "searching")
                viewModel.search(resized.first)
                Log.d("ImageCropper", "observing")
                viewModel.searchResult.observe(viewLifecycleOwner) {
                    it ?: return@observe
                    (binding.list.adapter as SearchRecyclerViewAdapter).updateProducts(it)
                }
                Log.d("ImageCropper", "search done")
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
                if (checkPermission()) {
                    takePicture.launch(uri)
                } else {
                    requestPermission()
                }
            }
            .setNegativeButton("گالری") { _, _ ->
                pickMedia.launch(PickVisualMediaRequest.Builder().build())
            }

        binding.searchButton.setOnClickListener {
            dialogBuilder.show()
        }
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

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    requireContext(),
                    "دسترسی داده‌شد",
                    Toast.LENGTH_LONG
                ).show()

                // main logic
            } else {
                Toast.makeText(
                    requireContext(),
                    "دسترسی داده‌نشد",
                    Toast.LENGTH_LONG
                ).show()
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    showMessageOKCancel(
                        "لطفا دسترسی دوربین را به دیجی‌ویژن بدهید",
                    ) { _, _ ->
                        requestPermission()
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("باشه", okListener)
            .setNegativeButton("نه", null)
            .create()
            .show()
    }

    private fun loadImageToByteArray(uri: Uri): ByteArray {
        requireContext().contentResolver.openInputStream(uri).use { inputStream ->
            return inputStream!!.readBytes()
        }
    }

    private fun resizeTheImage(
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

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}