package com.faceki.android.domain.preferences

import com.faceki.android.domain.model.RuleResponseData

internal interface Preferences {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveTokenType(tokenType: String)
    fun getTokenType(): String?
    fun saveTokenExpireTime(seconds: Int)
    fun getTokenExpireTime(): Int
    fun saveTokenTimestamp(millsInSeconds: Long)
    fun getTokenTimestamp(): Long
    fun saveRuleResponse(ruleResponse: RuleResponseData)
    fun getRuleResponse(): RuleResponseData

    companion object {
        const val KEY_TOKEN = "token"
        const val KEY_TOKEN_TYPE = "token_type"
        const val KEY_TOKEN_EXPIRE_TIME = "token_expire_time"
        const val KEY_TOKEN_TIMESTAMP = "token_timestamp"
        const val KEY_RULE_RESPONSE = "rule_response"
    }
}