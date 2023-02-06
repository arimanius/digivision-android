package edu.arimanius.digivision.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.protobuf.ByteString
import edu.arimanius.digivision.api.search.*
import edu.arimanius.digivision.data.dao.CategoryDao
import edu.arimanius.digivision.data.dao.HistoryDao
import edu.arimanius.digivision.data.dao.ProductHistoryDao
import edu.arimanius.digivision.data.entity.CategoryHistory
import edu.arimanius.digivision.data.entity.History
import edu.arimanius.digivision.data.entity.ProductHistory
import edu.arimanius.digivision.data.http.DigivisionRESTClient
import edu.arimanius.digivision.data.http.dto.HttpCropRequest
import edu.arimanius.digivision.data.http.dto.HttpCropResponse
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await
import java.io.IOException
import java.util.Base64

class SearchRepository(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val productHistoryDao: ProductHistoryDao,
    private val categoryDao: CategoryDao,
) {

    suspend fun cropREST(image: ByteArray): HttpCropResponse {
        val b64encoded = String(Base64.getEncoder().encode(image))
        Log.d("SearchRepository", "encoded: $b64encoded")
        val response = DigivisionRESTClient.getClient().getService().crop(
            HttpCropRequest(
                image = b64encoded
            )
        )
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
            this.params = searchParams {
                this.topK = 10
                this.ranker = Ranker.FIRST_IMAGE
            }
        })
        Log.d("SearchRepository", "search initiated")
        CoroutineScope(Dispatchers.IO).launch {
            // generate random string for image name
            val name = (0..10).map { ('a'..'z').random() }.joinToString("") + ".png"
            val uri = saveImage(image, name)
            Log.d("SearchRepository", "image saved to $uri")
            val historyId = historyDao.insert(
                History(
                    imageUri = uri.toString(),
                )
            )
            Log.d("SearchRepository", "history inserted with id $historyId")
            try {
                products.forEach { response ->
                    Log.d("SearchRepository", "product ${response.product.id} received")
                    productList.add(response.product)
                    result.postValue(productList)
                    val categoryIds = response.product.categoriesList.map { category ->
                        categoryDao.insert(
                            CategoryHistory(
                                url = category.url,
                                title = category.title,
                            )
                        )
                    }
                    productHistoryDao.insert(
                        ProductHistory(
                            historyId = historyId,
                            productId = response.product.id,
                            title = response.product.title,
                            url = response.product.url,
                            imageUrl = response.product.imageUrl,
                            status = response.product.status,
                            rate = response.product.rate.rate,
                            rateCount = response.product.rate.count,
                            categoryIds = categoryIds.joinToString(separator = ",") { it.toString() }
                        )
                    )
                }
            } finally {
                channel.shutdownNow()
            }
        }
        return result
    }

    fun getHistory(): LiveData<List<History>> {
        return historyDao.getAll()
    }

    fun searchInHistory(historyId: Long): LiveData<List<Product>> {
        val result = MutableLiveData<List<Product>>()
        CoroutineScope(Dispatchers.IO).launch {
            val productHistories = productHistoryDao.getByHistoryId(historyId)
            val products = productHistories.map {
                product {
                    id = it.productId
                    title = it.title
                    url = it.url
                    imageUrl = it.imageUrl
                    status = it.status
                    rate = rating {
                        it.rate
                        it.rateCount
                    }
                    categories.addAll(
                        it.categoryIds.split(",").mapNotNull { categoryId ->
                            val categoryHistory = categoryDao.getById(categoryId.toLong())
                            categoryHistory?.let {
                                category {
                                    url = categoryHistory.url
                                    title = categoryHistory.title
                                }
                            }
                        }
                    )
                }
            }
            result.postValue(products)
        }
        return result
    }

    private fun saveImage(image: ByteArray, name: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        }

        var uri: Uri? = null

        return runCatching {
            with(context.contentResolver) {
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                    uri = it // Keep uri reference so it can be removed on failure
                    val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
                    openOutputStream(it)?.use { stream ->
                        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream))
                            throw IOException("Failed to save bitmap.")
                    } ?: throw IOException("Failed to open output stream.")

                } ?: throw IOException("Failed to create new MediaStore record.")
            }
        }.getOrElse {
            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                context.contentResolver.delete(orphanUri, null, null)
            }

            throw it
        }
    }
}
