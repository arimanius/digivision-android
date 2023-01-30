package edu.arimanius.digivision.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.arimanius.digivision.data.repository.SearchRepository

class SearchViewModelFactory() : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(
                searchRepository = SearchRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
