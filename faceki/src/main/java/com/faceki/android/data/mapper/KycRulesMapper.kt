package com.faceki.android.data.mapper

import com.faceki.android.data.remote.dto.response.KycRulesResponseDataDto
import com.faceki.android.domain.model.RuleResponseData


internal fun KycRulesResponseDataDto.toRuleResponseData(): RuleResponseData {
    return RuleResponseData(
        id = id,
        companyId = companyId,
        allowSingle = allowSingle,
        allowedKycDocuments = allowedKycDocuments
    )
}

