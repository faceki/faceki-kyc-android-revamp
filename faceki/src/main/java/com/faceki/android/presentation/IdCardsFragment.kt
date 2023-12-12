package com.faceki.android.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentIdCardsBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.domain.model.CaptureItem
import com.faceki.android.presentation.adapter.CaptureItemsAdapter
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.util.Constants
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.isTrue
import com.faceki.android.util.loadIcon
import com.faceki.android.util.makeGone
import com.faceki.android.util.makeVisible
import com.faceki.android.util.navigateTo
import com.faceki.android.util.setBackgroundTintColor
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 */
internal class IdCardsFragment :
    BaseFragment<FragmentIdCardsBinding>(FragmentIdCardsBinding::inflate), View.OnClickListener {

    private var captureItems: ArrayList<CaptureItem> = arrayListOf()

    private var isSingleKyc: Boolean? = null
    private var requiredDocumentTypes: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            isSingleKyc = bundle.getBoolean(Constants.ARG_IS_SINGLE_KYC, false)

            requiredDocumentTypes = if (isSingleKyc.isTrue()) {
                bundle.getString(Constants.ARG_SELECTED_KYC_DOCUMENT)?.let {
                    arrayListOf(it)
                } ?: emptyList()
            } else {
                bundle.getStringArrayList(Constants.ARG_KYC_DOCUMENTS) ?: emptyList()
            }
        }
    }

    override fun setupViews() {

        binding.toolbar.apply {
            tv1.setText(R.string.verify_your_identity)
            tv2.setText(R.string.to_do_this_we_need_following)
        }

        lifecycleScope.launch {
            initializeCaptureItems(requiredDocumentTypes)
            binding.rvRequiredImages.adapter = CaptureItemsAdapter(captureItems)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.toolbar.btnBack.id -> {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            binding.bottomButton.btnNext.id -> {

                val args = Bundle().apply {
                    putParcelableArrayList(
                        Constants.ARG_CAPTURE_DOCUMENTS, captureItems
                    )
                }

                navController.navigateTo(
                    destinationId = R.id.idCardExampleFragment, args = args
                )
            }
        }
    }

    private fun initializeCaptureItems(documents: List<String>?) {
        captureItems.clear()
        if (documents == null) {
            return
        }
        documents.forEach { doc ->
            when (doc.trim().lowercase()) {
                Constants.PASSPORT.lowercase() -> {
                    captureItems.add(CaptureItem.DocumentFrontImage(doc))
                }

                else -> {
                    captureItems.add(CaptureItem.DocumentFrontImage(doc))
                    captureItems.add(CaptureItem.DocumentBackImage(doc))
                }
            }
        }
        captureItems.add(CaptureItem.Selfie)
    }
}