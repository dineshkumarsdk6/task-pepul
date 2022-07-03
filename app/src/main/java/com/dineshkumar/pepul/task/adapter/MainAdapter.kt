package com.dineshkumar.pepul.task.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dineshkumar.pepul.task.R
import com.dineshkumar.pepul.task.activity.ActivityVideoPlayer
import com.dineshkumar.pepul.task.activity.ActivityViewImage
import com.dineshkumar.pepul.task.databinding.LayoutInfoDialogBinding
import com.dineshkumar.pepul.task.databinding.LayoutRecycerViewItemBinding
import com.dineshkumar.pepul.task.databinding.LayoutRecycerViewItemLoadingBinding
import com.dineshkumar.pepul.task.interfaces.onClick
import com.dineshkumar.pepul.task.model.GetList
import com.dineshkumar.pepul.task.support.AppUtils

class MainAdapter(val context: Context, list: MutableList<GetList.Datum>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listAdapter = mutableListOf<GetList.Datum>()
    var listener = context as onClick

    init {
        this.listAdapter = list
        listener = context as onClick

    }

    fun setList() {
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {

        return if (listAdapter[position].id == "load") {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutRecycerViewItemLoadingBinding.inflate(inflater, parent, false)
                MainViewHolderLoader(binding)
            }
            1 -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutRecycerViewItemBinding.inflate(inflater, parent, false)
                MainViewHolder(binding)
            }
            else -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutRecycerViewItemBinding.inflate(inflater, parent, false)
                MainViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val list = listAdapter[position]

            if (list.fileType == "0") {
                holder.view.imageVideoPlay.visibility = View.GONE
                Glide.with(context)
                    .load(list.file)
                    .placeholder(R.drawable.image_preview)
                    .error(R.drawable.image_preview)
                    .transition(DrawableTransitionOptions.withCrossFade()) //Optional
                    .skipMemoryCache(false) //No memory cache
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //No disk cache
                    .into(holder.view.image)
            } else {
                holder.view.imageVideoPlay.visibility = View.VISIBLE
                Glide.with(context)
                    .load(list.file)
                    .transition(DrawableTransitionOptions.withCrossFade()) //Optional
                    .skipMemoryCache(false) //No memory cache
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //No disk cache
                    .into(holder.view.image)
            }

            holder.itemView.setOnClickListener {
                if (AppUtils.checkNetworkAvailable(context)) {
                    if (list.fileType == "0") {
                        val intent = Intent(context, ActivityViewImage::class.java)
                        intent.putExtra("url", list.file)
                        context.startActivity(intent)
                    } else {
                        val intent = Intent(context, ActivityVideoPlayer::class.java)
                        intent.putExtra("url", list.file)
                        context.startActivity(intent)
                    }
                } else {
                    AppUtils.showToast(context, AppUtils.text_net_check)
                }

            }

            holder.itemView.setOnLongClickListener {
                deleteWarning(context, list.id!!)
            }
        }


    }

    private fun deleteWarning(context: Context, itemId: String): Boolean {
        val dialog = Dialog(
            context,
            android.R.style.Theme_DeviceDefault_Dialog_MinWidth
        )
        val bindingDia: LayoutInfoDialogBinding =
            LayoutInfoDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(bindingDia.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        bindingDia.run {
            textTitle.text = context.getString(R.string.text_delete_title)
            textYes.text = context.getString(R.string.text_yes)
            textNo.text = context.getString(R.string.text_no)
        }

        bindingDia.textYes.setOnClickListener {
            dialog.dismiss()
            listener.onclick("" + itemId)
        }
        bindingDia.textNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
        return true
    }

    override fun getItemCount(): Int {
        println("== size : ${listAdapter.size}")
        return listAdapter.size
    }

    class MainViewHolder(val view: LayoutRecycerViewItemBinding) :
        RecyclerView.ViewHolder(view.root) {}

    class MainViewHolderLoader(val view: LayoutRecycerViewItemLoadingBinding) :
        RecyclerView.ViewHolder(view.root) {}

}