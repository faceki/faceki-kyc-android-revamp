package com.faceki.android

sealed class VerificationResult {
    data object ResultOk : VerificationResult()
    data object ResultCanceled : VerificationResult()
}
