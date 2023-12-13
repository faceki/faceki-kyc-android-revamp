package com.faceki.android

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class VerificationType : Parcelable {
    data object SingleKYC : VerificationType()
    data object MultipleKYC : VerificationType()
}
