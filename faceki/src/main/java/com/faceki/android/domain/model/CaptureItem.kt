package com.faceki.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class CaptureItem : Parcelable {

    @Parcelize
    data class DocumentFrontImage(val name: String) : CaptureItem()

    @Parcelize
    data class DocumentBackImage(val name: String) : CaptureItem()

    @Parcelize
    data object Selfie : CaptureItem()
}


