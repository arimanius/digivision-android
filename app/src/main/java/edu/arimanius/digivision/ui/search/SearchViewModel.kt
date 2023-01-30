package edu.arimanius.digivision.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.arimanius.digivision.api.search.CropResponse
import edu.arimanius.digivision.api.search.Product
import edu.arimanius.digivision.data.http.dto.HttpCropResponse
import edu.arimanius.digivision.data.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(val searchRepository: SearchRepository) : ViewModel() {
    var searchResult: LiveData<List<Product>> = MutableLiveData()

    fun cropREST(image: ByteArray): LiveData<HttpCropResponse> {
        val result = MutableLiveData<HttpCropResponse>()
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
        searchResult = searchRepository.search(image)
    }
}
