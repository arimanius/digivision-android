package edu.arimanius.digivision.data.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class HttpCropResponse(
    @JsonProperty("topLeft")
    val topLeft: HttpPosition,
    @JsonProperty("bottomRight")
    val bottomRight: HttpPosition
)
