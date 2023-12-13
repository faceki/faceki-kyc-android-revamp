package com.faceki.android.presentation.rule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceki.android.domain.repository.KycRuleRepository
import com.faceki.android.presentation.states.ScreenState
import com.faceki.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RuleViewModel(
    private val ruleRepository: KycRuleRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState get() = _screenState.asStateFlow()


    fun getRules(fetchFromRemote: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = ScreenState(
                isLoading = true, isSuccess = false
            )

            when (val resource = ruleRepository.getKycRules(fetchFromRemote)) {
                is Resource.Loading -> {}
                is Resource.Error -> {
                    Timber.e(resource.message)
                    _screenState.value = ScreenState(
                        isLoading = false, isSuccess = false
                    )
                }

                is Resource.Success -> {
                    _screenState.value = ScreenState(
                        isLoading = false, isSuccess = true, ruleData = resource.data
                    )
                }
            }
        }
    }

}