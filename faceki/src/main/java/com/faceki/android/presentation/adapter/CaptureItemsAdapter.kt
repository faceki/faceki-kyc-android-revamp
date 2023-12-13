package com.faceki.android.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.faceki.android.R
import com.faceki.android.databinding.ItemRequiredImagesBinding
import com.faceki.android.domain.model.CaptureItem
import com.faceki.android.presentation.base.BaseAdapter
import java.util.Locale

class CaptureItemsAdapter(
    captureItems: MutableList<CaptureItem>
) : BaseAdapter<ItemRequiredImagesBinding, CaptureItem>() {

    init {
        initDiffer(this@CaptureItemsAdapter)
        updateList(captureItems)
    }

    override fun getLayoutBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): ItemRequiredImagesBinding {
        return ItemRequiredImagesBinding.inflate(inflater, container, false)
    }

    override fun bindView(viewBinding: ItemRequiredImagesBinding, pos: Int, item: CaptureItem) {
        viewBinding.apply {
            tvSlNo.text = String.format(Locale.US, "%d", pos + 1)
            val context = root.context
            when (item) {
                is CaptureItem.DocumentFrontImage -> {
                    tvImageName.text =
                        context.getString(R.string.picture_of_your_s_front, item.name)
                }

                is CaptureItem.DocumentBackImage -> {
                    tvImageName.text = context.getString(R.string.picture_of_your_s_back, item.name)
                }

                is CaptureItem.Selfie -> {
                    tvImageName.text = context.getString(R.string.take_your_selfie)
                }
            }
        }
    }


}