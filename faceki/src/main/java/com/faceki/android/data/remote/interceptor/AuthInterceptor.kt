package com.faceki.android.data.remote.interceptor

import com.faceki.android.domain.provider.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newBuilder =
            originalRequest.newBuilder().addHeader(name = "Accept", value = "application/json")
                .addHeader(name = "Content-Type", value = "application/json")

        val token = tokenProvider.getToken()
        val newRequest = if (token != null) {
            newBuilder.addHeader(name = "Authorization", value = "Bearer $token")
        } else {
            newBuilder
        }.build()

        return chain.proceed(newRequest)
    }
}
