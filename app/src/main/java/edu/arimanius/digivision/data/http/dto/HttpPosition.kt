package edu.arimanius.digivision.data.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class HttpPosition(
    @JsonProperty("x")
    val x: Int,
    @JsonProperty("y")
    val y: Int
)
