package com.m2mmusic.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m2mmusic.android.databinding.ItemRecommendListBinding
import com.m2mmusic.android.logic.model.RecommendPlaylistsResponse.Result

class MainRecommendAdapter(private val listBean: List<Result>) :
    RecyclerView.Adapter<MainRecommendAdapter.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    inner class ViewHolder(binding: ItemRecommendListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val coverImage: ImageView = binding.cover
        val title: TextView = binding.title
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemRecommendListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (listBean.isNotEmpty()) {
            val playList = listBean[position]
            Glide.with(holder.itemView)
                .load(playList.picUrl + "?param=350y350")
                .into(holder.coverImage)
            holder.title.text = playList.name
            holder.itemView.setOnClickListener {
                mListener.onRecommendItemClick(position)
            }
        }

    }

    override fun getItemCount() = listBean.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        fun onRecommendItemClick(position: Int)
    }

}