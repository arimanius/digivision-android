package edu.arimanius.digivision.data.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CropResponse(
    @JsonProperty("topLeft")
    var topLeft: Position,
    @JsonProperty("bottomRight")
    var bottomRight: Position
)
