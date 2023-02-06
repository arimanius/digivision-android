package edu.arimanius.digivision.ui.search

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.arimanius.digivision.MainActivity
import edu.arimanius.digivision.databinding.FragmentSearchBinding


/**
 * A fragment representing a list of Items.
 */
class SearchFragment : SearchableFragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var columnCount = 1
    override val searchButton: View get() = binding.searchButton

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

        val bundle = arguments
        if (bundle != null) {
            when (bundle.getString("action")) {
                "history" -> {
                    (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.VISIBLE
                    val imageUri = bundle.getString("imageUri")
                    val historyId = bundle.getLong("historyId")
                    binding.imageView.setImageURI(Uri.parse(imageUri))
                    viewModel.getProductsByHistory(historyId).observe(viewLifecycleOwner) {
                        it ?: return@observe
                        (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.GONE
                        (binding.list.adapter as SearchRecyclerViewAdapter).updateProducts(it)
                    }
                }
                "search" -> {
                    (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.VISIBLE
                    val imageUri = Uri.parse(bundle.getString("imageUri"))
                    binding.imageView.setImageURI(imageUri)
                    val image = loadImageToByteArray(imageUri)
                    Log.d("ImageCropper", "searching")
                    viewModel.search(image)
                    Log.d("ImageCropper", "observing")
                    (binding.list.adapter as SearchRecyclerViewAdapter).clearProducts()
                    viewModel.searchResult.observe(viewLifecycleOwner) {
                        it ?: return@observe
                        (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.GONE
                        (binding.list.adapter as SearchRecyclerViewAdapter).updateProducts(it)
                    }
                    Log.d("ImageCropper", "search done")
                }
            }
        }
    }

    override fun onImageCropped(uri: Uri) {
        binding.imageView.setImageURI(uri)
        val image = loadImageToByteArray(uri)
        Log.d("ImageCropper", "searching")
        viewModel.search(image)
        Log.d("ImageCropper", "observing")
        (binding.list.adapter as SearchRecyclerViewAdapter).clearProducts()
        viewModel.searchResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            (binding.list.adapter as SearchRecyclerViewAdapter).updateProducts(it)
        }
        Log.d("ImageCropper", "search done")
    }
}