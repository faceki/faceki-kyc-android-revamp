package com.faceki.android.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentSingleKycIdSelectionBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.presentation.adapter.SingleSelectableKycDocumentsAdapter
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.presentation.rule.RuleViewModel
import com.faceki.android.presentation.rule.RuleViewModelFactory
import com.faceki.android.util.Constants
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.isNotNull
import com.faceki.android.util.isNull
import com.faceki.android.util.isTrue
import com.faceki.android.util.loadIcon
import com.faceki.android.util.makeGone
import com.faceki.android.util.makeVisible
import com.faceki.android.util.navigateTo
import com.faceki.android.util.setBackgroundTintColor
import com.faceki.android.util.showToast
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 */
internal class SingleKycIdSelectionFragment :
    BaseFragment<FragmentSingleKycIdSelectionBinding>(FragmentSingleKycIdSelectionBinding::inflate),
    View.OnClickListener {

    private var kycDocumentsAdapter: SingleSelectableKycDocumentsAdapter? = null

    private val ruleViewModel: RuleViewModel by viewModels {
        RuleViewModelFactory()
    }

    override fun setupViews() {

        binding.toolbar.apply {
            tv1.setText(R.string.verify_your_identity)
            tv2.setText(R.string.select_a_document_type)
        }

        binding.bottomButton.btnNext.setText(R.string.next)

        lifecycleScope.launch {
            ruleViewModel.getRules(fetchFromRemote = false)
        }
    }

    override fun setupThemes() {
        AppConfig.getCustomColor(FaceKi.ColorElement.BackgroundColor).getColorIntOrNull()?.let {
            binding.root.setBackgroundColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.TitleTextColor).getColorIntOrNull()?.let {
            binding.toolbar.tv1.setTextColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.SecondaryTextColor).getColorIntOrNull()?.let {
            binding.toolbar.tv2.setTextColor(it)
        }

        AppConfig.getCustomColor(FaceKi.ColorElement.ButtonBackgroundColor).getColorIntOrNull()
            ?.let {
                binding.bottomButton.btnNext.setBackgroundTintColor(it)
            }
        AppConfig.getCustomColor(FaceKi.ColorElement.ButtonTextColor).getColorIntOrNull()?.let {
            binding.bottomButton.btnNext.setTextColor(it)
            binding.bottomButton.btnNext.iconTint = ColorStateList.valueOf(it)
        }

        val logoImgView = binding.toolbar.imageViewLogo
        logoImgView.loadIcon(iconValue = AppConfig.getCustomIcon(FaceKi.IconElement.Logo),
            onSuccess = { logoImgView.makeVisible() },
            onFailure = { exception ->
                logoImgView.makeGone()
                exception.printStackTrace()
            })


    }

    override fun setupClickListeners() {
        binding.toolbar.btnBack.setOnClickListener(this)
        binding.bottomButton.btnNext.setOnClickListener(this)
    }

    override fun observeData() {
        lifecycleScope.launch {
            ruleViewModel.screenState.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            ).collect { state ->
                if (state.isSuccess.isTrue() && state.ruleData.isNotNull()) {

                    val data = state.ruleData!!
                    if (data.allowSingle.isNull()) {
                        activity?.onBackPressedDispatcher?.onBackPressed()
                        return@collect
                    }

                    val selectedKycDoc = kycDocumentsAdapter?.selectedKycDocument
                    kycDocumentsAdapter =
                        SingleSelectableKycDocumentsAdapter(data.allowedKycDocuments)
                    selectedKycDoc?.let {
                        kycDocumentsAdapter?.selectedKycDocument = selectedKycDoc
                    }

                    binding.rvIdCards.adapter = kycDocumentsAdapter
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.bottomButton.btnNext.id -> {
                if (kycDocumentsAdapter.isNull() or kycDocumentsAdapter!!.selectedKycDocument.isNull()) {
                    showToast("Please select a Kyc Document")
                    return
                }

                val args = Bundle().apply {
                    putBoolean(Constants.ARG_IS_SINGLE_KYC, true)
                    putString(
                        Constants.ARG_SELECTED_KYC_DOCUMENT,
                        kycDocumentsAdapter!!.selectedKycDocument
                    )
                }

                navController.navigateTo(
                    destinationId = R.id.idCardsFragment,
                    args = args
                )
            }

            binding.toolbar.btnBack.id -> {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

}