package com.faceki.android.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faceki.android.di.AppModule
import com.faceki.android.domain.repository.TokenRepository

class TokenViewModelFactory(private val tokenRepository: TokenRepository = AppModule.provideTokenRepository()) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TokenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return TokenViewModel(tokenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
