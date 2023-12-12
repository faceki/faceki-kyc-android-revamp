package com.faceki.android.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faceki.android.di.AppModule
import com.faceki.android.domain.repository.KycVerificationRepository


class KycVerificationViewModelFactory(
    private val kycVerificationRepository: KycVerificationRepository = AppModule.provideKycVerificationRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KycVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return KycVerificationViewModel(kycVerificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
