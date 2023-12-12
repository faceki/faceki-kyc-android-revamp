package com.faceki.android.presentation.base


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * @param viewBinding The Item Layout File View Binding
 * @param T data class type
 */
abstract class BaseAdapter<viewBinding : ViewBinding, T : Any> :
    RecyclerView.Adapter<BaseAdapter<viewBinding, T>.MyViewHolder>() {

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            areItemsSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
            areContentsSame(oldItem, newItem)
    }
    private lateinit var differ: AsyncListDiffer<T>

    fun initDiffer(adapter: BaseAdapter<viewBinding, T>) {
        differ = AsyncListDiffer(adapter, diffUtilCallBack)
    }


    abstract fun getLayoutBinding(inflater: LayoutInflater, container: ViewGroup?): viewBinding

    abstract fun bindView(viewBinding: viewBinding, pos: Int, item: T)

    open fun areItemsSame(oldItem: T, newItem: T): Boolean =
        oldItem.toString() == newItem.toString()

    open fun areContentsSame(oldItem: T, newItem: T): Boolean = oldItem == newItem


    fun getCurrentList(): List<T> = differ.currentList

    fun updateList(updated: List<T>) {
        if (updated.isEmpty()) {
            differ.submitList(emptyList())
            return
        }
        differ.submitList(updated)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(getLayoutBinding(LayoutInflater.from(parent.context), parent))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition
        val item = differ.currentList[pos]
        bindView(holder.viewBinding, pos, item)
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class MyViewHolder(val viewBinding: viewBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

}