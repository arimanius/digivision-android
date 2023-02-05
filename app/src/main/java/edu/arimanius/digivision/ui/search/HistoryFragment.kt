package edu.arimanius.digivision.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.arimanius.digivision.R
import edu.arimanius.digivision.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchViewModel
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
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(requireContext())
        )[SearchViewModel::class.java]

        viewModel.getHistory().observe(viewLifecycleOwner) {
            (binding.historyList.adapter as HistoryRecyclerViewAdapter).updateHistories(it)
        }

        binding.searchButton.setOnClickListener {
            findNavController().navigate(R.id.action_historyFragment_to_searchFragment)
        }
    }
}