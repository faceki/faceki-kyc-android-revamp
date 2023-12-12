package com.faceki.android.domain.provider

interface TokenProvider {
    fun getToken(): String?
    fun getTokenType(): String?
    fun getTokenExpireTime(): Int
    fun getTokenTimestamp(): Long
}
