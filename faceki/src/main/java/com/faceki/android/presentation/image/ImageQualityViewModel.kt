package com.faceki.android.presentation.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceki.android.domain.repository.ImageQualityRepository
import com.faceki.android.presentation.states.ScreenState
import com.faceki.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


class ImageQualityViewModel(
    private val imageQualityRepository: ImageQualityRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState get() = _screenState.asStateFlow()

    fun checkImageQuality(image: File) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = ScreenState(
                isLoading = true
            )

            when (val resource = imageQualityRepository.checkImageQuality(image)) {
                is Resource.Loading -> {
                }

                is Resource.Error -> {
                    Timber.e(resource.message)
                    _screenState.value = ScreenState(
                        isLoading = false, isSuccess = false, errorMessage = resource.message
                    )
                }

                is Resource.Success -> {
                    _screenState.value = ScreenState(
                        isLoading = false, isSuccess = true
                    )
                }
            }
        }
    }
}