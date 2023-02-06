package edu.arimanius.digivision.ui.history

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.arimanius.digivision.R
import edu.arimanius.digivision.databinding.FragmentHistoryBinding
import edu.arimanius.digivision.ui.search.SearchableFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryFragment : SearchableFragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    override val searchButton: View get() = binding.searchButton
    private var columnCount = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Set the adapter
        with(binding.historyList) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = HistoryRecyclerViewAdapter()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getHistory().observe(viewLifecycleOwner) {
            (binding.historyList.adapter as HistoryRecyclerViewAdapter).updateHistories(it)
        }
    }

    override fun onImageCropped(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            val bundle = Bundle()
            bundle.putString("action", "search")
            bundle.putString("imageUri", uri.toString())
            findNavController().navigate(R.id.action_historyFragment_to_searchFragment, bundle)
        }
    }
}