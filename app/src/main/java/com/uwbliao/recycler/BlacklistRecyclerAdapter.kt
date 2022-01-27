package com.uwbliao.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.uwbliao.databinding.BlacklistItemBinding
import com.uwbliao.db.RepDevice
import kotlinx.coroutines.launch

class BlacklistRecyclerAdapter : LifecycleOwner, RecyclerView.Adapter<BlacklistRecyclerAdapter.ResultHolder>() {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle() = lifecycleRegistry

    private var itemsList: MutableList<BlacklistRecyclerItem> = arrayListOf()

    fun clearItems() {
        itemsList.clear()
        notifyDataSetChanged()
    }

    fun setItems(mutableList: MutableList<BlacklistRecyclerItem>) {
        if (mutableList != itemsList) {
            itemsList = mutableList
            notifyDataSetChanged()
        }
    }

    fun addSingleItem(item: BlacklistRecyclerItem) {
        itemsList.removeAll {
            it.DeviceName == item.DeviceName
        }
        itemsList.add(item)
        notifyDataSetChanged()
    }

    fun removeSingleItem(item: BlacklistRecyclerItem) {
        itemsList.removeAll {
            it.DeviceName == item.DeviceName
        }
        notifyDataSetChanged()
    }

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int): BlacklistRecyclerItem? = if (itemsList.isEmpty()) null else itemsList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
        val binding = BlacklistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: called for position $position")
        holder.bind(getItem(position))
    }

    inner class ResultHolder(private val binding: BlacklistItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private var _item: BlacklistRecyclerItem? = null

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(item: BlacklistRecyclerItem?) {
            item?.let {
                binding.txtDeviceName.text = it.DeviceName
                binding.txtNickname.text = it.Nickname
                binding.txtGender.text = it.Gender
            }
            //this.itemView.setBackgroundColor(Color.parseColor("#ff4d4d"))
            _item = item
        }
        override fun onClick(v: View) {
            //remove from list
            itemsList.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
            notifyItemRangeChanged(adapterPosition, itemsList.size)
            //save to db
            val repdev = RepDevice(_item?.DeviceName!!)
            repdev.entityDevice!!.hide = false
            _item?.RemoteDev!!.hide = false
            lifecycleScope.launch { repdev.updateDevice(repdev.entityDevice) }
        }
    }

    companion object {
        private val TAG = BlacklistRecyclerAdapter::class.java.simpleName
    }
}
