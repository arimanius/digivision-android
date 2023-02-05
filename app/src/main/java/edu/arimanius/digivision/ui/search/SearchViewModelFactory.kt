package edu.arimanius.digivision.ui.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.arimanius.digivision.data.DigivisionDatabase
import edu.arimanius.digivision.data.repository.SearchRepository

class SearchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(
                searchRepository = SearchRepository(
                    context,
                    DigivisionDatabase.getInstance(context).historyDao(),
                    DigivisionDatabase.getInstance(context).productHistoryDao(),
                    DigivisionDatabase.getInstance(context).categoryDao(),
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
