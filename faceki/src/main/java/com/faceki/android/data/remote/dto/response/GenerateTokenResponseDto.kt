package com.faceki.android.data.remote.dto.response

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
internal data class GenerateTokenResponseDto(
    @field:Json(name = "responseCode") val responseCode: Int? = null,
    @field:Json(name = "data") val data: GenerateTokenResponseDataDto? = null,
    @field:Json(name = "status") val status: Boolean? = null,
    @field:Json(name = "message") val message: String = "",
    @field:Json(name = "statusCode") val statusCode: Int? = null,
    @field:Json(name = "clientSecret") val clientSecret: String? = null
)

@Keep
internal data class GenerateTokenResponseDataDto(
    @field:Json(name = "access_token") val accessToken: String? = null,
    @field:Json(name = "expires_in") val expiresIn: Int = -1,
    @field:Json(name = "token_type") val tokenType: String? = null
)