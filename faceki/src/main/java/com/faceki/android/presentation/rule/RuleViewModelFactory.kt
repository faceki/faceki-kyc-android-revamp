package com.faceki.android.presentation.rule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faceki.android.di.AppModule
import com.faceki.android.domain.repository.KycRuleRepository

class RuleViewModelFactory(
    private val ruleRepository: KycRuleRepository = AppModule.provideKycRuleRepository()
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return RuleViewModel(ruleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
