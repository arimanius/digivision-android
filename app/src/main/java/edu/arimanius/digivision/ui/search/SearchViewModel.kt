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
    private val _error: MutableLiveData<String?> = MutableLiveData(null)
    val error: LiveData<String?> get() = _error

    fun cropREST(image: ByteArray): LiveData<CropResponse> {
        val result = MutableLiveData<CropResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                result.value = searchRepository.cropREST(image)
            } catch (e: Exception) {
                _error.postValue("خطا در بارگذاری تصویر")
            }
        }
        return result
    }

    fun clearError() {
        _error.postValue(null)
    }

    fun crop(image: ByteArray): LiveData<CropResponse> {
        val result = MutableLiveData<CropResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                result.value = searchRepository.crop(image)
            } catch (e: Exception) {
                _error.postValue("خطا در بارگذاری تصویر")
            }
        }
        return result
    }

    fun search(image: ByteArray) {
        val result = searchRepository.search(image) {
            _error.postValue(it.message)
        }
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

    fun deleteHistory(historyId: Long) {
        searchRepository.deleteHistory(historyId)
    }
}
