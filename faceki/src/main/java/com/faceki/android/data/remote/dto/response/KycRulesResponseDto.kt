package com.faceki.android.data.remote.dto.response

import androidx.annotation.Keep
import com.squareup.moshi.Json


@Keep
internal data class KycRulesResponseDto(
    @field:Json(name = "responseCode") val responseCode: Int? = null,
    @field:Json(name = "data") val data: KycRulesResponseDataDto? = null
)

@Keep
internal data class KycRulesResponseDataDto(
    @field:Json(name = "_id") val id: String? = null,
    @field:Json(name = "companyId") val companyId: String? = null,
    @field:Json(name = "allowSingle") val allowSingle: Boolean? = null,
    @field:Json(name = "allowedKycDocuments") val allowedKycDocuments: List<String> = emptyList()
)