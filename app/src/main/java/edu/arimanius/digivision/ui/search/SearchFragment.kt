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
    private var imageUri: Uri? = null
    private var historyId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            imageUri = Uri.parse(bundle.getString("imageUri"))
            (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.VISIBLE
            when (bundle.getString("action")) {
                "history" -> {
                    historyId = bundle.getLong("historyId")
                    viewModel.getProductsByHistory(historyId)
                }
                "search" -> {
                    val image = loadImageToByteArray(imageUri!!)
                    Log.d("ImageCropper", "searching")
                    viewModel.search(image)
                    Log.d("ImageCropper", "search done")
                }
            }
        }
    }

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

        binding.imageView.setImageURI(imageUri)
        viewModel.searchResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.GONE
            (binding.list.adapter as SearchRecyclerViewAdapter).updateProducts(it)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            it ?: return@observe
            (binding.list.adapter as SearchRecyclerViewAdapter).updateLoading(it)
        }
    }

    override fun onImageCropped(uri: Uri) {
        imageUri = uri
        binding.imageView.setImageURI(uri)
        viewModel.search(loadImageToByteArray(uri))
        (binding.list.adapter as SearchRecyclerViewAdapter).clearProducts()
        (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.VISIBLE
        viewModel.searchResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            (requireActivity() as MainActivity).binding.loadingPanel.visibility = View.GONE
            (binding.list.adapter as SearchRecyclerViewAdapter).updateProducts(it)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            it ?: return@observe
            (binding.list.adapter as SearchRecyclerViewAdapter).updateLoading(it)
        }
        Log.d("ImageCropper", "search done")
    }
}