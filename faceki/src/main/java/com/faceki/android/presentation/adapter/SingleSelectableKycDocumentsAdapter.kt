package com.faceki.android.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.faceki.android.databinding.ItemSelectableIdCardBinding
import com.faceki.android.presentation.base.BaseAdapter

class SingleSelectableKycDocumentsAdapter(
    allowedKycDocuments: List<String>
) : BaseAdapter<ItemSelectableIdCardBinding, String>() {

    var selectedKycDocument: String? = null

    init {
        initDiffer(this@SingleSelectableKycDocumentsAdapter)
        if (allowedKycDocuments.isNotEmpty()) {
            selectedKycDocument = allowedKycDocuments[0]
        }
        updateList(allowedKycDocuments)
    }

    override fun getLayoutBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): ItemSelectableIdCardBinding {
        return ItemSelectableIdCardBinding.inflate(inflater, container, false)
    }

    override fun bindView(viewBinding: ItemSelectableIdCardBinding, pos: Int, item: String) {
        viewBinding.apply {
            tvCardName.text = item
            radioBtn.setOnCheckedChangeListener(null)
            radioBtn.isChecked = item == selectedKycDocument
            radioBtn.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedKycDocument = item
                    notifyDataSetChanged()
                }
            }
            root.setOnClickListener {
                radioBtn.isChecked = true
            }
        }
    }

}