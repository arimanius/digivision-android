package edu.arimanius.digivision.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.arimanius.digivision.api.search.Product
import edu.arimanius.digivision.data.entity.History
import edu.arimanius.digivision.data.http.dto.CropResponse
import edu.arimanius.digivision.data.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(val searchRepository: SearchRepository) : ViewModel() {
    var searchResult: LiveData<List<Product>> = MutableLiveData()
    var isLoading: LiveData<Boolean> = MutableLiveData()

    fun cropREST(image: ByteArray): LiveData<CropResponse> {
        val result = MutableLiveData<CropResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            result.value = searchRepository.cropREST(image)
        }
        return result
    }

    fun crop(image: ByteArray): LiveData<CropResponse> {
        val result = MutableLiveData<CropResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            result.value = searchRepository.crop(image)
        }
        return result
    }

    fun search(image: ByteArray) {
        val result = searchRepository.search(image)
        searchResult = result.first
        isLoading = result.second
    }

    fun getHistory(): LiveData<List<History>> {
        return searchRepository.getHistory()
    }

    fun getProductsByHistory(historyId: Long) {
        searchResult = searchRepository.searchInHistory(historyId)
        isLoading = MutableLiveData(false)
    }
}
