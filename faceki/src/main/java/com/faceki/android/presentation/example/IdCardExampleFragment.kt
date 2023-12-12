package com.faceki.android.presentation.example

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentIdCardExampleBinding
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
internal class IdCardExampleFragment :
    BaseFragment<FragmentIdCardExampleBinding>(FragmentIdCardExampleBinding::inflate),
    View.OnClickListener {

    private var captureItems: ArrayList<CaptureItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            captureItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelableArrayList(
                    Constants.ARG_CAPTURE_DOCUMENTS, CaptureItem::class.java
                )
            } else {
                bundle.getParcelableArrayList(Constants.ARG_CAPTURE_DOCUMENTS)
            } ?: arrayListOf()
        }
    }

    override fun setupViews() {
        binding.toolbar.apply {
            tv1.setText(R.string.verify_your_id_card)
            tv2.setText(R.string.you_need_to_take_a_photo)
        }
        binding.bottomButton.btnNext.setText(R.string.i_m_ready)
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

        val guidanceImgView = binding.guidanceImgView
        val validImageview = binding.validImageview
        guidanceImgView.loadIcon(iconValue = AppConfig.getCustomIcon(FaceKi.IconElement.GuidanceImage),
            onSuccess = {
                guidanceImgView.makeVisible()
                validImageview.makeGone()
            },
            onFailure = { exception ->
                guidanceImgView.setImageResource(R.drawable.example_invalid_id_cards)
                validImageview.makeVisible()
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
                navController.navigateTo(
                    destinationId = R.id.cameraFragment,
                    args = Bundle().apply {
                        putParcelableArrayList(
                            Constants.ARG_CAPTURE_DOCUMENTS, captureItems
                        )
                        putInt(Constants.ARG_CAPTURE_POSITION, 0)
                    })
            }
        }
    }
}