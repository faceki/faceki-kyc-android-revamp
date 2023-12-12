package com.faceki.android.domain.repository

import com.faceki.android.util.Resource

interface TokenRepository {
    suspend fun getBearerToken(clientId: String, clientSecret: String): Resource<String>
    fun getBearerToken(): String?
    fun getTokenType(): String?
    fun getTokenExpireTime(): Int
    fun getTokenTimestamp(): Long
}