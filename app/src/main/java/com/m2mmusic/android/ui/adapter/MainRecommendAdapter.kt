package com.m2mmusic.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m2mmusic.android.databinding.ItemRecommendListBinding
import com.m2mmusic.android.logic.model.Result

class MainRecommendAdapter(private val listBean: List<Result>) :
    RecyclerView.Adapter<MainRecommendAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(binding: ItemRecommendListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val coverImage: ImageView = binding.cover
        val title: TextView = binding.title
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PagerViewHolder {
        val binding =
            ItemRecommendListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = PagerViewHolder(binding)
        viewHolder.itemView.setOnClickListener {
            // 启动ListDetailActivity显示歌单内容
//            startListDetailActivity(this, it.tag)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        if (listBean.isNotEmpty()) {
            val playList = listBean[position]
            Glide.with(holder.itemView)
                .load(playList.picUrl)
                .into(holder.coverImage)
            holder.title.text = playList.name
            // 将歌单id存入view的tag中，当item被点击时将数据传给ListDetailActivity
            holder.itemView.tag = playList.id
        }

    }

    override fun getItemCount() = listBean.size

}