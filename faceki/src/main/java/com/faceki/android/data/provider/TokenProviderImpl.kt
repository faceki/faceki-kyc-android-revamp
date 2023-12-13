package com.faceki.android.data.provider

import com.faceki.android.domain.preferences.Preferences
import com.faceki.android.domain.provider.TokenProvider

internal class TokenProviderImpl(
    private val preferences: Preferences
) : TokenProvider {
    override fun getToken(): String? = preferences.getToken()

    override fun getTokenType(): String? = preferences.getTokenType()

    override fun getTokenExpireTime(): Int = preferences.getTokenExpireTime()

    override fun getTokenTimestamp(): Long = preferences.getTokenTimestamp()
}