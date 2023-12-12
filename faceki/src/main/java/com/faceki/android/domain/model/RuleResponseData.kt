package com.faceki.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RuleResponseData(
    val id: String? = null,
    val companyId: String? = null,
    val allowSingle: Boolean? = null,
    val allowedKycDocuments: List<String> = emptyList()
) : Parcelable
