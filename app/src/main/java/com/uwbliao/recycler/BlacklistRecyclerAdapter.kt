package com.uwbliao.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uwbliao.R
import com.uwbliao.databinding.BlacklistDspBinding
import com.uwbliao.databinding.BlacklistItemBinding
import com.uwbliao.db.RepDevice
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class BlacklistRecyclerAdapter(private val blacklistDspBinding: BlacklistDspBinding):
    LifecycleOwner, RecyclerView.Adapter<BlacklistRecyclerAdapter.ResultHolder>() {

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

    inner class ResultHolder(private val binding: BlacklistItemBinding):
        RecyclerView.ViewHolder(binding.root),
//        View.OnClickListener,
        View.OnTouchListener {

        private var _item: BlacklistRecyclerItem? = null

        init {
//            binding.root.setOnClickListener(this)
            binding.root.setOnTouchListener(this)
        }

        fun bind(item: BlacklistRecyclerItem?) {
            item?.let {
                binding.txtDeviceName.text = it.DeviceName
                binding.txtNickname.text = it.Nickname
                binding.txtGender.text = it.Gender
            }
            //this.itemView.setBackgroundColor(Color.parseColor("#ff4d4d"))
            _item = item
            //move item x coordinate back to original position
            super.itemView.x = 0f
        }

//        override fun onClick(v: View) {
//
//        }

        private var touchStartX = 0f
        override fun onTouch(v: View, e: MotionEvent): Boolean {
//            // variables to store current configuration of blacklist dialog
//            val displayMetrics = v.context.resources.displayMetrics
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartX = e.rawX
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(e.rawX - touchStartX)
                        .setDuration(0).start()
                }
//                MotionEvent.ACTION_UP -> {
//                    val movedX = v.x//moved x distance
//                    if(movedX.absoluteValue >= MIN_REMOVE_SWIPE_DISTANCE) { //moved horizontally
//                    v.animate().x(dlgStart).setDuration(150)
//                        .setListener(
//                            object : AnimatorListenerAdapter() {
//                                override fun onAnimationEnd(animation: Animator) {
//                                    lifecycleScope.launch(Dispatchers.Default) {
//                                        delay(100)
//                                    }
//                                }
//                            }
//                        )
//                        .start()
//                        v.x = 0f// move back to original position
//                        removeBlacklistItem(v)
//                    } else {
//                        //move back to original position
//                        v.animate().x(0f).setDuration(150).start()
//                    }
//                    if(movedX.absoluteValue > 0) removeBlacklistItem(v)
//                    touchStartX = 0f//reinitialization
//                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    val movedX = v.x//moved x distance
//                    v.x = 0f//move back to original position
                    if(movedX.absoluteValue > 0) removeBlacklistItem(v)
                    touchStartX = 0f//reinitialization
                }
            }
            v.performClick()// required to by-pass lint warning
            return true
        }

        private fun removeBlacklistItem(v: View) {
              val removedPos = adapterPosition
            //remove from list
            itemsList.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
            notifyItemRangeChanged(adapterPosition, itemsList.size)
            //save to db
            val repdev = RepDevice(_item?.DeviceName!!)
            repdev.entityDevice!!.hide = false
            _item?.RemoteDev!!.hide = false
            lifecycleScope.launch { repdev.updateDevice(repdev.entityDevice) }
            //undo remove action
            val undoSnackbar = Snackbar.make(blacklistDspBinding.undoCoordinatorLayout, //binding.root.rootView,
                v.context.resources.getString(R.string.txt_blacklist_item_remove_msg),
                Snackbar.LENGTH_SHORT)
            undoSnackbar.setAction(
                v.context.resources.getString(R.string.txt_undo_blacklist_item_remove_msg),
                UndoRemoveListener(repdev, removedPos, _item!!)
            )
//            undoSnackbar.setAnchorView(mainView).show()
            undoSnackbar.setAnchorView(blacklistDspBinding.undoCoordinatorLayout).show()
        }

        inner class UndoRemoveListener(
            repdev: RepDevice, removedPos: Int, item: BlacklistRecyclerItem) : View.OnClickListener {
            private val _repdev = repdev
            private val _removedPos = removedPos
            private val _item = item
            override fun onClick(v: View) {
                //undo remove blacklist item
                itemsList.add(_removedPos, _item)
                notifyItemRangeChanged(_removedPos, itemsList.size)
                //save to db
                _repdev.entityDevice!!.hide = true
                _item.RemoteDev.hide = true
                lifecycleScope.launch { _repdev.updateDevice(_repdev.entityDevice) }
            }
        }

    }

    companion object {
        private val TAG = BlacklistRecyclerAdapter::class.java.simpleName
//        const val MIN_REMOVE_SWIPE_DISTANCE = 50//item will be removed at least this distance was moved
    }
}
