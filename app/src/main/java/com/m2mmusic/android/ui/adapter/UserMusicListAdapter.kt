package com.m2mmusic.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m2mmusic.android.databinding.ItemMyMusicListBinding
import com.m2mmusic.android.logic.model.PlaylistDetailResponse

class UserMusicListAdapter(private val listBean: List<PlaylistDetailResponse.Playlist>) :
    RecyclerView.Adapter<UserMusicListAdapter.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    inner class ViewHolder(binding: ItemMyMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val musicListCover: ImageView = binding.myMusicListCover
        val musicListTitle: TextView = binding.myMusicListTitle
        val musicListCount: TextView = binding.myMusicListCount
        val musicListCreator: TextView = binding.myMusicListCreator
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMyMusicListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val musicList = listBean[position]
        Glide.with(holder.itemView).load(musicList.coverImgUrl + "?param=300y300")
            .into(holder.musicListCover)
        holder.apply {
            musicListTitle.text = musicList.name
            musicListCount.text = musicList.trackCount.toString()
            musicListCreator.text = musicList.creator.nickname
            itemView.tag = musicList.id
            itemView.setOnClickListener {
                mListener.onUserMusicListItemClick(position)
            }
        }
    }

    override fun getItemCount() = listBean.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        fun onUserMusicListItemClick(position: Int)
    }

}