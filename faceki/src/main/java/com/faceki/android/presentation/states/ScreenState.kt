package com.faceki.android.presentation.states

import com.faceki.android.domain.model.RuleResponseData


data class ScreenState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val isSuccess: Boolean? = null,
    val ruleData: RuleResponseData? = null,
    val verificationResponse: String? = null,
    val responseCode: Int? = null,
    val errorMessage: String? = null
)