package com.faceki.android.presentation.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faceki.android.di.AppModule
import com.faceki.android.domain.repository.ImageQualityRepository


class ImageQualityViewModelFactory(
    private val imageQualityRepository: ImageQualityRepository = AppModule.provideImageQualityRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageQualityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ImageQualityViewModel(imageQualityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
