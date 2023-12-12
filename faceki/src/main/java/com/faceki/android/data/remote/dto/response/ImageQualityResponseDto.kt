package com.faceki.android.data.remote.dto.response

import androidx.annotation.Keep
import com.squareup.moshi.Json


@Keep
internal data class ImageQualityResponseDto(
    @field:Json(name = "objects_detected") val objectsDetected: List<String> = emptyList(),
    @field:Json(name = "quality") val quality: String? = null,
    @field:Json(name = "liveness") val liveNess: LiveNess? = null
)

@Keep
internal data class LiveNess(
    @field:Json(name = "actual") val actual: Boolean? = null,
    @field:Json(name = "livenessScore") val liveNessScore: String? = null,
    @field:Json(name = "message") val message: String? = null
)

