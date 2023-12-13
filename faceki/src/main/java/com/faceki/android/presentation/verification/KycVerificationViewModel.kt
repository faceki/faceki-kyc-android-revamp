package com.faceki.android.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceki.android.domain.repository.KycVerificationRepository
import com.faceki.android.presentation.states.ScreenState
import com.faceki.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


class KycVerificationViewModel(
    private val kycVerificationRepository: KycVerificationRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState get() = _screenState.asStateFlow()

    fun verifySingleKyc(
        selfieImage: File, docFrontImage: File, docBackImage: File
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = ScreenState(
                isLoading = true
            )

            val resource = kycVerificationRepository.verifySingleKyc(
                selfieImage = selfieImage,
                docFrontImage = docFrontImage,
                docBackImage = docBackImage
            )

            when (resource) {
                is Resource.Loading -> {
                }

                is Resource.Error -> {
                    Timber.e(resource.message)
                    _screenState.value = ScreenState(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = resource.message,
                        responseCode = resource.data?.responseCode
                    )
                }

                is Resource.Success -> {
                    _screenState.value = ScreenState(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = resource.data?.errorMessage,
                        verificationResponse = resource.data?.jsonBody,
                        responseCode = resource.data?.responseCode
                    )
                }
            }
        }

    }

    fun verifyMultipleKyc(
        selfieImage: File,
        idFrontImage: File,
        idBackImage: File,
        dlFrontImage: File?,
        dlBackImage: File?,
        ppFrontImage: File?,
        ppBackImage: File?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = ScreenState(
                isLoading = true
            )

            val resource = kycVerificationRepository.verifyMultipleKyc(
                selfieImage = selfieImage,
                idFrontImage = idFrontImage,
                idBackImage = idBackImage,
                dlFrontImage = dlFrontImage,
                dlBackImage = dlBackImage,
                ppFrontImage = ppFrontImage,
                ppBackImage = ppBackImage
            )

            when (resource) {
                is Resource.Loading -> {
                }

                is Resource.Error -> {
                    Timber.e(resource.message)
                    _screenState.value = ScreenState(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = resource.message, responseCode = resource.data?.responseCode
                    )
                }

                is Resource.Success -> {
                    _screenState.value = ScreenState(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = resource.data?.errorMessage,
                        verificationResponse = resource.data?.jsonBody,
                        responseCode = resource.data?.responseCode
                    )
                }
            }
        }

    }

}