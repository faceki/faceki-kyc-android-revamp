package com.faceki.android.presentation.example

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentSelfieExampleBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.domain.model.CaptureItem
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.util.Constants
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.loadIcon
import com.faceki.android.util.makeGone
import com.faceki.android.util.makeVisible
import com.faceki.android.util.navigateTo
import com.faceki.android.util.setBackgroundTintColor


/**
 * A simple [Fragment] subclass.
 */
internal class SelfieExampleFragment :
    BaseFragment<FragmentSelfieExampleBinding>(FragmentSelfieExampleBinding::inflate),
    View.OnClickListener {

    private var captureItem: CaptureItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            captureItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(
                    Constants.ARG_CAPTURE_SINGLE_DOCUMENT, CaptureItem::class.java
                )
            } else {
                bundle.getParcelable(Constants.ARG_CAPTURE_SINGLE_DOCUMENT)
            }
        }
    }

    override fun setupViews() {
        binding.toolbar.apply {
            tv1.setText(R.string.verify_that_it_s_you)
            tv2.setText(R.string.take_a_selfie)
        }
        binding.bottomButton.btnNext.setText(R.string.i_m_ready)
    }

    override fun setupThemes() {
        AppConfig.getCustomColor(FaceKi.ColorElement.BackgroundColor).getColorIntOrNull()?.let {
            binding.root.setBackgroundColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.TitleTextColor).getColorIntOrNull()?.let {
            binding.toolbar.tv1.setTextColor(it)
            binding.tvTitle2.setTextColor(it)
        }
        AppConfig.getCustomColor(FaceKi.ColorElement.SecondaryTextColor).getColorIntOrNull()?.let {
            binding.toolbar.tv2.setTextColor(it)
            binding.tvDesc1.setTextColor(it)
            binding.tvDesc2.setTextColor(it)
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
                navController.navigateTo(destinationId = R.id.cameraFragment,
                    args = Bundle().apply {
                        putParcelable(Constants.ARG_CAPTURE_SINGLE_DOCUMENT, captureItem)
                        putBoolean(Constants.ARG_IS_CAPTURE_SELFIE, true)
                    })
            }
        }
    }

}