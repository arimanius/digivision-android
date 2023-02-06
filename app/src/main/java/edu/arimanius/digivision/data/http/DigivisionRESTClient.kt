package edu.arimanius.digivision.data.http

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class DigivisionRESTClient private constructor(private val retrofit: Retrofit) {

    companion object {
        private var instance: DigivisionRESTClient? = null
        fun getClient(): DigivisionRESTClient {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            return instance ?: synchronized(this) {
                instance ?: DigivisionRESTClient(
                    Retrofit.Builder()
                        .baseUrl("http://digivision.chicheca.ir:8082/api/v1/")
                        .addConverterFactory(JacksonConverterFactory.create())
                        .client(okHttpClient)
                        .build()
                ).also { instance = it }
            }
        }
    }

    fun getService(): DigivisionRESTService =
        retrofit.create(DigivisionRESTService::class.java)
}
