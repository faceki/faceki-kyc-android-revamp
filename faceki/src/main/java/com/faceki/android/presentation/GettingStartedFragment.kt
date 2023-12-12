package com.faceki.android.presentation

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentGettingStartedBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.domain.model.RuleResponseData
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.util.Constants
import com.faceki.android.util.VerificationType
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.isNetworkNotConnected
import com.faceki.android.util.isNotNull
import com.faceki.android.util.isTrue
import com.faceki.android.util.navigateTo
import com.faceki.android.util.setBackgroundTintColor
import com.faceki.android.util.showToast
import com.faceki.android.util.toArrayList


/**
 * A simple [Fragment] subclass.
 */
internal class GettingStartedFragment :
    BaseFragment<FragmentGettingStartedBinding>(FragmentGettingStartedBinding::inflate),
    View.OnClickListener {

    private var ruleData: RuleResponseData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ruleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(Constants.ARG_RULE_RESPONSE, RuleResponseData::class.java)
            } else {
                it.getParcelable(Constants.ARG_RULE_RESPONSE)
            }
        }
    }

    override fun setupViews() {

    }

    override fun setupClickListeners() {
        binding.bottomButton.btnNext.setOnClickListener(this)
    }

    override fun setupThemes() {
        AppConfig.getCustomColor(FaceKi.ColorElement.BackgroundColor).getColorIntOrNull()?.let {
            binding.root.setBackgroundColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.TitleTextColor).getColorIntOrNull()?.let {
            binding.tvGettingStarted.setTextColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.PrimaryTextColor).getColorIntOrNull()?.let {
            binding.tvTitle2.setTextColor(it)
            binding.tvTitle3.setTextColor(it)
        }

        AppConfig.getCustomColor(FaceKi.ColorElement.SecondaryTextColor).getColorIntOrNull()?.let {
            binding.tvDesc1.setTextColor(it)
        }


        AppConfig.getCustomColor(FaceKi.ColorElement.ButtonBackgroundColor).getColorIntOrNull()
            ?.let {
                binding.bottomButton.btnNext.setBackgroundTintColor(it)
            }
        AppConfig.getCustomColor(FaceKi.ColorElement.ButtonTextColor).getColorIntOrNull()?.let {
            binding.bottomButton.btnNext.setTextColor(it)
            binding.bottomButton.btnNext.iconTint = ColorStateList.valueOf(it)
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.bottomButton.btnNext.id -> {
                if (isNetworkNotConnected()) {
                    showToast("Please connect to internet")
                    return
                }
                if (isLoading) {
                    return
                }

                if (ruleData.isNotNull()) {
                    if (ruleData?.allowSingle.isTrue()) {
                        AppConfig.verificationType = VerificationType.SingleKYC
                        navController.navigateTo(destinationId = R.id.singleKycIdSelectionFragment)
                    } else {
                        AppConfig.verificationType = VerificationType.MultipleKYC
                        navController.navigateTo(destinationId = R.id.idCardsFragment,
                            args = Bundle().apply {
                                putBoolean(Constants.ARG_IS_SINGLE_KYC, false)
                                putStringArrayList(
                                    Constants.ARG_KYC_DOCUMENTS,
                                    ruleData!!.allowedKycDocuments.toArrayList()
                                )
                            })
                    }
                }

            }
        }
    }

}