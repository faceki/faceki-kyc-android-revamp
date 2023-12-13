package com.faceki.android.presentation

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.VerificationResult
import com.faceki.android.VerificationType
import com.faceki.android.databinding.FragmentLottieAnimationBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.presentation.verification.KycVerificationViewModel
import com.faceki.android.presentation.verification.KycVerificationViewModelFactory
import com.faceki.android.presentation.welcome.TokenViewModel
import com.faceki.android.presentation.welcome.TokenViewModelFactory
import com.faceki.android.util.Constants
import com.faceki.android.util.KYCErrorCodes
import com.faceki.android.util.asFile
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.isNetworkNotConnected
import com.faceki.android.util.isTrue
import com.faceki.android.util.navigateTo
import com.faceki.android.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 */
internal class LottieAnimationFragment :
    BaseFragment<FragmentLottieAnimationBinding>(FragmentLottieAnimationBinding::inflate) {

    private var verificationResponse: String? = null
    private lateinit var lottieAnimationType: LottieAnimationType

    private val tokenViewModel: TokenViewModel by viewModels {
        TokenViewModelFactory()
    }

    private var token: String? = null

    private val kycVerificationViewModel: KycVerificationViewModel by viewModels {
        KycVerificationViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationResponse = it.getString(Constants.ARG_VERIFICATION_RESPONSE)
            lottieAnimationType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(
                    Constants.ARG_LOTTIE_ANIMATION_TYPE, LottieAnimationType::class.java
                )
            } else {
                it.getParcelable(Constants.ARG_LOTTIE_ANIMATION_TYPE)
            }!!
            Timber.tag(TAG).d(lottieAnimationType.toString())
        }
    }


    override fun setupViews() {
        setAnimationAndTexts()
        bindingNullable?.root?.apply {
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { _, keyCode, _ ->
                keyCode == KeyEvent.KEYCODE_BACK
            }
        }

        lifecycleScope.launch {
            if (lottieAnimationType == LottieAnimationType.VERIFICATION_LOADING) {
                getToken()
            } else {
                removeAllFragmentsExceptCurrent()

                withContext(Dispatchers.IO) {
                    delay(ANIMATION_DURATION)
                }
                activity?.finishAndRemoveTask()
                AppConfig.kycResponseHandler?.handleKycResponse(
                    json = verificationResponse,
                    type = AppConfig.verificationType,
                    result = VerificationResult.ResultOk
                )
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            if (lottieAnimationType == LottieAnimationType.VERIFICATION_LOADING) {
                kycVerificationViewModel.screenState.flowWithLifecycle(
                    lifecycle, Lifecycle.State.STARTED
                ).collect { state ->

                    if (state.responseCode == KYCErrorCodes.SUCCESS) {
                        navController.navigateTo(destinationId = R.id.lottieAnimationFragment,
                            args = Bundle().apply {
                                putString(
                                    Constants.ARG_VERIFICATION_RESPONSE, state.verificationResponse
                                )
                                putParcelable(
                                    Constants.ARG_LOTTIE_ANIMATION_TYPE, LottieAnimationType.SUCCESS
                                )
                            })
                    } else if (state.responseCode == KYCErrorCodes.PLEASE_TRY_AGAIN || state.responseCode == KYCErrorCodes.FACE_CROPPED || state.responseCode == KYCErrorCodes.FACE_TOO_CLOSED || state.responseCode == KYCErrorCodes.FACE_NOT_FOUND || state.responseCode == KYCErrorCodes.FACE_CLOSED_TO_BORDER || state.responseCode == KYCErrorCodes.FACE_TOO_SMALL || state.responseCode == KYCErrorCodes.POOR_LIGHT) {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            Constants.ARG_VERIFICATION_RESPONSE, state.errorMessage
                        )
                        activity?.apply {
                            runOnUiThread {
                                navController.navigateUp()
                            }
                        }
                    } else if (state.responseCode != null) {
                        navController.navigateTo(
                            destinationId = R.id.lottieAnimationFragment,
                            args = Bundle().apply {
                                putString(
                                    Constants.ARG_VERIFICATION_RESPONSE, state.verificationResponse
                                )
                                putParcelable(
                                    Constants.ARG_LOTTIE_ANIMATION_TYPE, LottieAnimationType.FAIL
                                )
                            })
                    }
                }
            }
        }
        lifecycleScope.launch {
            if (lottieAnimationType == LottieAnimationType.VERIFICATION_LOADING) {
                tokenViewModel.screenState.flowWithLifecycle(
                    lifecycle, Lifecycle.State.STARTED
                ).collect { state ->
                    if (state.isSuccess.isTrue() && state.token?.isNotBlank().isTrue()) {
                        if (token == null || token != state.token) {
                            token = state.token

                            verifyKycDocuments()
                        }
                    }
                }
            }
        }
    }

    private fun getToken() {
        lifecycleScope.launch {
            token = null
            if (isNetworkNotConnected()) {
                showToast("Please connect to internet")
                activity?.onBackPressedDispatcher?.onBackPressed()
                return@launch
            }
            val clientId = AppConfig.clientId!!
            val clientSecret = AppConfig.clientSecret!!
            tokenViewModel.getBearerToken(
                clientId = clientId, clientSecret = clientSecret
            )
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

    private fun setAnimationAndTexts() {
        when (lottieAnimationType) {
            LottieAnimationType.SUCCESS -> {
                binding.lottieAnimView.setAnimation(R.raw.lottie_success)
                binding.tvLoadingVerification.visibility = View.GONE
                binding.llSuccessFail.visibility = View.VISIBLE
                binding.tvTitle.setText(R.string.successful)
                binding.tvDesc.setText(R.string.verification_successful_msg)
            }

            LottieAnimationType.FAIL -> {
                binding.lottieAnimView.setAnimation(R.raw.lottie_fail)
                binding.tvLoadingVerification.visibility = View.GONE
                binding.llSuccessFail.visibility = View.VISIBLE
                binding.tvTitle.setText(R.string.failed)
                binding.tvDesc.setText(R.string.verification_failed_msg)
            }

            LottieAnimationType.VERIFICATION_LOADING -> {
                binding.lottieAnimView.setAnimation(R.raw.lottie_loading)
                binding.llSuccessFail.visibility = View.GONE
                binding.tvLoadingVerification.visibility = View.VISIBLE
            }
        }
    }

    private fun removeAllFragmentsExceptCurrent() {
        val currentDestinationId = navController.currentDestination?.id ?: return
        navController.popBackStack(currentDestinationId, false)
    }


    private fun verifyKycDocuments() {
        when (AppConfig.verificationType!!) {
            is VerificationType.SingleKYC -> {
                kycVerificationViewModel.verifySingleKyc(
                    selfieImage = AppConfig.getImagePath(Constants.SELFIE)!!.asFile(),
                    docFrontImage = AppConfig.getImagePath(Constants.ID_CARD_FRONT_IMAGE)!!
                        .asFile(),
                    docBackImage = AppConfig.getImagePath(Constants.ID_CARD_BACK_IMAGE)!!.asFile()
                )
            }

            is VerificationType.MultipleKYC -> {

                Timber.d(AppConfig.getImagePath(Constants.SELFIE)!!.asFile().absolutePath)
                Timber.d(
                    AppConfig.getImagePath(Constants.ID_CARD_FRONT_IMAGE)!!.asFile().absolutePath
                )
                Timber.d(
                    AppConfig.getImagePath(Constants.ID_CARD_BACK_IMAGE)!!.asFile().absolutePath
                )
                Timber.d(AppConfig.getImagePath(Constants.DL_FRONT_IMAGE)?.asFile()?.absolutePath)
                Timber.d(AppConfig.getImagePath(Constants.DL_BACK_IMAGE)?.asFile()?.absolutePath)
                Timber.d(AppConfig.getImagePath(Constants.PP_FRONT_IMAGE)?.asFile()?.absolutePath)
                Timber.d(AppConfig.getImagePath(Constants.PP_BACK_IMAGE)?.asFile()?.absolutePath)

                kycVerificationViewModel.verifyMultipleKyc(
                    selfieImage = AppConfig.getImagePath(Constants.SELFIE)!!.asFile(),
                    idFrontImage = AppConfig.getImagePath(Constants.ID_CARD_FRONT_IMAGE)!!.asFile(),
                    idBackImage = AppConfig.getImagePath(Constants.ID_CARD_BACK_IMAGE)!!.asFile(),
                    dlFrontImage = AppConfig.getImagePath(Constants.DL_FRONT_IMAGE)?.asFile(),
                    dlBackImage = AppConfig.getImagePath(Constants.DL_BACK_IMAGE)?.asFile(),
                    ppFrontImage = AppConfig.getImagePath(Constants.PP_FRONT_IMAGE)?.asFile(),
                    ppBackImage = AppConfig.getImagePath(Constants.PP_BACK_IMAGE)?.asFile()
                )
            }
        }

    }


    companion object {
        private const val ANIMATION_DURATION = 3000L
        private const val TAG = "LottieAnimationFragment"
    }
}

@Parcelize
enum class LottieAnimationType : Parcelable {
    SUCCESS, FAIL, VERIFICATION_LOADING
}

