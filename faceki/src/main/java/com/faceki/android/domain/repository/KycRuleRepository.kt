package com.faceki.android.domain.repository

import com.faceki.android.domain.model.RuleResponseData
import com.faceki.android.util.Resource

interface KycRuleRepository {
    suspend fun getKycRules(fetchFromRemote: Boolean): Resource<RuleResponseData>
}