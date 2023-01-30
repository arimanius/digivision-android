package edu.arimanius.digivision.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.protobuf.ByteString
import edu.arimanius.digivision.api.search.*
import edu.arimanius.digivision.data.http.DigivisionRESTClient
import edu.arimanius.digivision.data.http.dto.HttpCropRequest
import edu.arimanius.digivision.data.http.dto.HttpCropResponse
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await
import java.util.Base64

class SearchRepository {

    suspend fun cropREST(image: ByteArray): HttpCropResponse {
        val b64encoded = String(Base64.getEncoder().encode(image))
        Log.d("SearchRepository", "encoded: $b64encoded")
        val response = DigivisionRESTClient.getClient().getService().crop(HttpCropRequest(
            image = b64encoded
        ))
        Log.d("SearchRepository", "response")
        return withContext(Dispatchers.IO) {
            response.await()
        }
    }

    suspend fun crop(image: ByteArray): CropResponse {
        val channel = ManagedChannelBuilder.forAddress("arvan5.bettercallme.ir", 8081)
            .enableRetry()
            .usePlaintext()
            .build()
        val stub = SearchServiceGrpc.newFutureStub(channel)
        val response = stub.crop(cropRequest {
            this.image = ByteString.copyFrom(image)
        })
        try {
            return withContext(Dispatchers.IO) {
                response.get()
            }
        } finally {
            channel.shutdownNow()
        }
    }

    fun search(image: ByteArray): LiveData<List<Product>> {
        val result = MutableLiveData<List<Product>>()
        val productList = mutableListOf<Product>()
        val channel = ManagedChannelBuilder.forAddress("arvan5.bettercallme.ir", 8081)
            .enableRetry()
            .usePlaintext()
            .build()
        val stub = SearchServiceGrpc.newBlockingStub(channel)
        val products = stub.asyncSearch(searchRequest {
            this.image = ByteString.copyFrom(image)
            this.topK = 100
        })
        Log.d("SearchRepository", "search initiated")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                products.forEach { response ->
                    Log.d("SearchRepository", "product ${response.product.id} received")
                    productList.add(response.product)
                    result.postValue(productList)
                }
            } finally {
                channel.shutdownNow()
            }
        }
        return result
    }
}
