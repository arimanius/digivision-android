package edu.arimanius.digivision.data.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Position(
    @JsonProperty("x")
    var x: Int,
    @JsonProperty("y")
    var y: Int
)
