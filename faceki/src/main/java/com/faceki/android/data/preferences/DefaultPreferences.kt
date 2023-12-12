package com.faceki.android.data.preferences

import android.content.SharedPreferences
import com.faceki.android.domain.model.RuleResponseData
import com.faceki.android.domain.preferences.Preferences
import com.squareup.moshi.Moshi
import timber.log.Timber

internal class DefaultPreferences(
    private val sharedPref: SharedPreferences
) : Preferences {
    override fun saveToken(token: String) {
        sharedPref.edit().putString(Preferences.KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return sharedPref.getString(Preferences.KEY_TOKEN, null)
    }

    override fun saveTokenType(tokenType: String) {
        sharedPref.edit().putString(Preferences.KEY_TOKEN_TYPE, tokenType).apply()
    }

    override fun getTokenType(): String? {
        return sharedPref.getString(Preferences.KEY_TOKEN_TYPE, null)
    }

    override fun saveTokenExpireTime(seconds: Int) {
        sharedPref.edit().putInt(Preferences.KEY_TOKEN_EXPIRE_TIME, seconds).apply()
    }

    override fun getTokenExpireTime(): Int {
        return sharedPref.getInt(Preferences.KEY_TOKEN_EXPIRE_TIME, -1)
    }

    override fun saveTokenTimestamp(millsInSeconds: Long) {
        sharedPref.edit().putLong(Preferences.KEY_TOKEN_TIMESTAMP, millsInSeconds).apply()
    }

    override fun getTokenTimestamp(): Long {
        return sharedPref.getLong(Preferences.KEY_TOKEN_TIMESTAMP, -1)
    }

    override fun saveRuleResponse(ruleResponse: RuleResponseData) {
        try {
            val jsonAdapter = Moshi.Builder().build().adapter(RuleResponseData::class.java)
            val json = jsonAdapter.toJson(ruleResponse)
            sharedPref.edit().putString(Preferences.KEY_RULE_RESPONSE, json).apply()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getRuleResponse(): RuleResponseData {
        return try {
            val jsonAdapter = Moshi.Builder().build().adapter(RuleResponseData::class.java)
            val json = sharedPref.getString(Preferences.KEY_RULE_RESPONSE, null)
                ?: return RuleResponseData()
            jsonAdapter.fromJson(json)!!
        } catch (e: Exception) {
            Timber.e(e)
            RuleResponseData()
        }
    }


}