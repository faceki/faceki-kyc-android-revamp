package com.faceki.android.data.remote

import com.faceki.android.data.remote.dto.response.GenerateTokenResponseDto
import com.faceki.android.data.remote.dto.response.KycRulesResponseDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


internal interface FaceKiApi {

    @GET(GENERATE_TOKEN)
    suspend fun generateToken(
        @Query("clientId") clientId: String,
        @Query("clientSecret") clientSecret: String,
    ): Response<GenerateTokenResponseDto>

    @GET(KYC_RULES)
    suspend fun getKycRules(): Response<KycRulesResponseDto>

    @POST(SINGLE_KYC_VERIFICATION)
    @Multipart
    suspend fun verifySingleKyc(
        @Part selfie_image: MultipartBody.Part,
        @Part doc_front_image: MultipartBody.Part,
        @Part doc_back_image: MultipartBody.Part
    ): Response<ResponseBody>

    @POST(MULTIPLE_KYC_VERIFICATION)
    @Multipart
    suspend fun verifyMultipleKyc(
        @Part selfie_image: MultipartBody.Part,
        @Part id_front_image: MultipartBody.Part,
        @Part id_back_image: MultipartBody.Part,
        @Part dl_front_image: MultipartBody.Part?,
        @Part dl_back_image: MultipartBody.Part?,
        @Part pp_front_image: MultipartBody.Part?,
        @Part pp_back_image: MultipartBody.Part?
    ): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://sdk.faceki.com/"
        const val GENERATE_TOKEN = "auth/api/access-token"
        const val KYC_RULES = "kycrules/api/kycrules"
        const val SINGLE_KYC_VERIFICATION = "kycverify/api/kycverify/kyc-verification"
        const val MULTIPLE_KYC_VERIFICATION = "kycverify/api/kycverify/multi-kyc-verification"
    }
}