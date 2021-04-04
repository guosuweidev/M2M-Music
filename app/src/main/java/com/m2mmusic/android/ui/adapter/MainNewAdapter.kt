package com.m2mmusic.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m2mmusic.android.databinding.ItemNewListBinding
import com.m2mmusic.android.logic.model.NewResourcesResponse.Creative
import java.lang.StringBuilder

class MainNewAdapter(
    private val listBean: List<Creative>,
) : RecyclerView.Adapter<MainNewAdapter.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (listBean.isNotEmpty()) {
            val song = listBean[position / 3].resources[position % 3]
            Glide.with(holder.itemView)
                .load(song.uiElement.image.imageUrl + "?param=300y300")
                .into(holder.coverImage)
            holder.mainTitle.text = song.uiElement.mainTitle.title
            holder.subTitle.text = song.uiElement.subTitle.title
            holder.artist.text = song.resourceExtInfo.artists.run {
                var i = this.size - 1
                var artists: StringBuilder = StringBuilder(this[0].name)
                for (artist in this) {
                    if (i > 0) {
                        artists.append("/").append(artist.name)
                        i--
                    }
                }
                artists
            }
            holder.itemView.tag = song.resourceId
            holder.itemView.setOnClickListener {
                mListener.onNewResItemClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return 6
    }

    inner class ViewHolder(binding: ItemNewListBinding) : RecyclerView.ViewHolder(binding.root) {
        val coverImage: ImageView = binding.newResCover
        val mainTitle: TextView = binding.newResMainTitle
        val subTitle: TextView = binding.newResSubTitle
        val artist: TextView = binding.newResArtists
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        fun onNewResItemClick(position: Int)
    }

}