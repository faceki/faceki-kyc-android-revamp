package com.faceki.android.domain.repository

import com.faceki.android.domain.model.VerificationResponse
import com.faceki.android.util.Resource
import java.io.File

interface KycVerificationRepository {
    suspend fun verifySingleKyc(
        selfieImage: File, docFrontImage: File, docBackImage: File
    ): Resource<VerificationResponse>

    suspend fun verifyMultipleKyc(
        selfieImage: File,
        idFrontImage: File,
        idBackImage: File,
        dlFrontImage: File? = null,
        dlBackImage: File? = null,
        ppFrontImage: File? = null,
        ppBackImage: File? = null
    ): Resource<VerificationResponse>
}