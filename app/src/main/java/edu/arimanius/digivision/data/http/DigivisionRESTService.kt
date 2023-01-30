package edu.arimanius.digivision.data.http

import edu.arimanius.digivision.data.http.dto.HttpCropRequest
import edu.arimanius.digivision.data.http.dto.HttpCropResponse
import retrofit2.Call
import retrofit2.http.*

interface DigivisionRESTService {
    @Headers("Content-type: application/json")
    @POST("crop")
    fun crop(
        @Body request: HttpCropRequest
    ): Call<HttpCropResponse>
}
