package com.faceki.android.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceki.android.domain.repository.TokenRepository
import com.faceki.android.presentation.states.ScreenState
import com.faceki.android.util.Resource
import com.faceki.android.util.hasTokenExpired
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class TokenViewModel constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState get() = _screenState.asStateFlow()


    fun getBearerToken(clientId: String, clientSecret: String) {
        viewModelScope.launch(Dispatchers.IO) {

            _screenState.value = ScreenState(
                isLoading = true, token = null, isSuccess = false
            )



            when (val resource = tokenRepository.getBearerToken(
                clientId = clientId, clientSecret = clientSecret
            )) {
                is Resource.Loading -> {}
                is Resource.Error -> {
                    Timber.e(resource.message)
                    _screenState.value = ScreenState(
                        isLoading = false, token = null, isSuccess = false
                    )
                }

                is Resource.Success -> {
                    Timber.i("token ${resource.data}")
                    _screenState.value = ScreenState(
                        isLoading = false, token = resource.data, isSuccess = true
                    )
                }

            }
        }


        fun hasTokenExpired(): Boolean {
            return tokenRepository.getTokenTimestamp().hasTokenExpired(
                token = tokenRepository.getBearerToken(),
                expiresInSec = tokenRepository.getTokenExpireTime()
            )
        }

    }
}