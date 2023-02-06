package edu.arimanius.digivision.ui.history

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import edu.arimanius.digivision.R
import edu.arimanius.digivision.databinding.FragmentHistoryBinding
import edu.arimanius.digivision.ui.search.SearchableFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        if (includeEdge) {
            outRect.left =
                spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right =
                (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
            outRect.right =
                spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}

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
        if (columnCount > 1) {
            binding.historyList.addItemDecoration(GridSpacingItemDecoration(columnCount, 3, false))
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