package com.faceki.android.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentSplashBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.di.AppModule
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.presentation.rule.RuleViewModel
import com.faceki.android.presentation.rule.RuleViewModelFactory
import com.faceki.android.presentation.welcome.TokenViewModel
import com.faceki.android.presentation.welcome.TokenViewModelFactory
import com.faceki.android.util.Constants
import com.faceki.android.util.FileManager
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.isNetworkNotConnected
import com.faceki.android.util.isNotNull
import com.faceki.android.util.isTrue
import com.faceki.android.util.navigateTo
import com.faceki.android.util.showToast
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 */
internal class SplashFragment :
    BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val tokenViewModel: TokenViewModel by viewModels {
        TokenViewModelFactory()
    }

    private var token: String? = null
    private var allowSingle: Boolean? = null

    private val ruleViewModel: RuleViewModel by viewModels {
        RuleViewModelFactory()
    }

    override fun setupViews() {
        activity?.application?.let {
            AppModule.initialize(it)
            FileManager.initialize(it)
            FileManager.deleteAllFiles()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isLoading) {
            return
        }
        getToken()
    }

    override fun observeData() {
        lifecycleScope.launch {
            tokenViewModel.screenState.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            ).collect { state ->
                if (state.isSuccess.isTrue() && state.token?.isNotBlank().isTrue()) {
                    if (token == null) {
                        observeRules()
                        token = state.token
                        isLoading = true
                        ruleViewModel.getRules(fetchFromRemote = true)
                    } else if (token != state.token) {
                        token = state.token
                        isLoading = true
                        ruleViewModel.getRules(fetchFromRemote = true)
                    }
                } else {
                    isLoading = false
                }
            }
        }
    }

    override fun setupThemes() {
        AppConfig.getCustomColor(FaceKi.ColorElement.BackgroundColor).getColorIntOrNull()?.let {
            binding.root.setBackgroundColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.TitleTextColor).getColorIntOrNull()?.let {
            binding.tvTitle.setTextColor(it)
        }
    }

    private fun getToken() {
        lifecycleScope.launch {
            if (isNetworkNotConnected()) {
                showToast("Please connect to internet")
                return@launch
            }
            isLoading = true
            val clientId = AppConfig.clientId!!
            val clientSecret = AppConfig.clientSecret!!
            tokenViewModel.getBearerToken(
                clientId = clientId, clientSecret = clientSecret
            )
        }
    }

    private fun observeRules() {
        lifecycleScope.launch {
            ruleViewModel.screenState.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            ).collect { state ->
                if (state.isSuccess.isTrue() && state.ruleData.isNotNull()) {
                    val data = state.ruleData!!
                    allowSingle = data.allowSingle

                    val args = Bundle().apply {
                        putParcelable(Constants.ARG_RULE_RESPONSE, data)
                    }
                    navController.navigateTo(
                        R.id.gettingStartedFragment, popUpFragId = R.id.splashFragment, args = args
                    )
                }
                isLoading = false
            }
        }
    }

}