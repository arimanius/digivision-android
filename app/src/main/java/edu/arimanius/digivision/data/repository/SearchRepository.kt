package edu.arimanius.digivision.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
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
import edu.arimanius.digivision.data.http.dto.CropResponse
import edu.arimanius.digivision.data.http.dto.HttpCropRequest
import edu.arimanius.digivision.data.http.dto.Position
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await
import java.io.*
import java.util.*


class SearchRepository(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val productHistoryDao: ProductHistoryDao,
    private val categoryDao: CategoryDao,
) {

    suspend fun cropREST(image: ByteArray): CropResponse {
        val resized = resizeTheImage(image, 512)
        val b64encoded = String(Base64.getEncoder().encode(resized.first))
        Log.d("SearchRepository", "encoded: $b64encoded")
        val response = DigivisionRESTClient.getClient().getService().crop(
            HttpCropRequest(
                image = b64encoded
            )
        )
        Log.d("SearchRepository", "response")
        return withContext(Dispatchers.IO) {
            val cropped = response.await()
            cropped.topLeft.x = (cropped.topLeft.x / resized.second).toInt()
            cropped.topLeft.y = (cropped.topLeft.y / resized.second).toInt()
            cropped.bottomRight.x = (cropped.bottomRight.x / resized.second).toInt()
            cropped.bottomRight.y = (cropped.bottomRight.y / resized.second).toInt()
            cropped
        }
    }

    suspend fun crop(image: ByteArray): CropResponse {
        val channel = ManagedChannelBuilder.forAddress("arvan5.bettercallme.ir", 8081)
            .enableRetry()
            .usePlaintext()
            .build()
        try {
            val stub = SearchServiceGrpc.newFutureStub(channel)
            val resized = resizeTheImage(image, 512)
            val response = stub.crop(cropRequest {
                this.image = ByteString.copyFrom(resized.first)
            })
            return withContext(Dispatchers.IO) {
                val cropped = response.get()
                CropResponse(
                    topLeft = Position(
                        x = (cropped.topLeft.x / resized.second).toInt(),
                        y = (cropped.topLeft.y / resized.second).toInt(),
                    ),
                    bottomRight = Position(
                        x = (cropped.bottomRight.x / resized.second).toInt(),
                        y =  (cropped.bottomRight.y / resized.second).toInt()
                    ),
                )
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
        val resized = resizeTheImage(image, 256, 256)
        val products = stub.asyncSearch(searchRequest {
            this.image = ByteString.copyFrom(resized.first)
            this.params = searchParams {
                this.topK = 40
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
        var uri: Uri? = null

        return runCatching {
            val file = File(context.getExternalFilesDir(null), name)
            uri = file.toUri()
            val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
            FileOutputStream(file).use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream))
                    throw IOException("Failed to save bitmap.")
            }
            uri!!
        }.getOrElse {
            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                context.contentResolver.delete(orphanUri, null, null)
            }
            throw it
        }
    }

    private fun resizeTheImage(
        bytes: ByteArray?,
        w: Int = -1,
        h: Int = -1
    ): Pair<ByteArray, Float> {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
        Log.d("PhotoPicker", "Original size: ${bitmap.width}x${bitmap.height}")
        var width = w
        var height = h
        var scale = width.toFloat() / bitmap.width.toFloat()
        if (width == -1) {
            assert(height != -1)
            scale = height.toFloat() / bitmap.height.toFloat()
            val ratio = bitmap.height.toFloat() / h.toFloat()
            width = (bitmap.width / ratio).toInt()
        } else if (h == -1) {
            assert(width != -1)
            val ratio = w.toFloat() / bitmap.width.toFloat()
            height = (bitmap.height * ratio).toInt()
        }
        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)
        Log.d("PhotoPicker", "Resized size: ${resized.width}x${resized.height}")
        Log.d("PhotoPicker", "Scale: $scale")
        return Pair(bitmapToByteArray(resized), scale)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
