package com.faceki.android.domain.repository

import com.faceki.android.util.Resource
import java.io.File

interface ImageQualityRepository {
    suspend fun checkImageQuality(
        image: File
    ): Resource<String>
}