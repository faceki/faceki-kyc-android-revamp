package com.faceki.android

interface KycResponseHandler {
    fun handleKycResponse(json: String?, type: VerificationType?, result: VerificationResult)
}
