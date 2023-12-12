package com.faceki.android.data.remote

import com.faceki.android.data.remote.dto.response.ImageQualityResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface ImageQualityApi {

    @POST(QUALITY_CHECK)
    @Multipart
    suspend fun checkQuality(
        @Part image: MultipartBody.Part
    ): Response<ImageQualityResponseDto>

    companion object {
        const val BASE_URL = "https://addon.faceki.com/"
        const val QUALITY_CHECK = "advance/detect"
    }
}