package com.m2mmusic.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.m2mmusic.android.databinding.ItemMusicListBinding
import com.m2mmusic.android.logic.model.Music

class PlayListAdapter(private val listBean: ArrayList<Music>, private var nowPlaying: Int = -1) :
    RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener


    inner class ViewHolder(binding: ItemMusicListBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemCount: TextView = binding.count
        val title: TextView = binding.musicName
        val artists: TextView = binding.musicArtist
        val alia: TextView = binding.alia
        val isPlaySign: ImageView = binding.isPlayingIcon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMusicListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = listBean[position]
        holder.apply {
            if (nowPlaying == position) {
                itemCount.text = ""
                isPlaySign.visibility = View.VISIBLE
            } else {
                itemCount.text = (position + 1).toString()
                isPlaySign.visibility = View.GONE
            }

            title.text = music.title
//            alia.text = music.alia
            alia.text = ""
            artists.text = music.artist

            itemView.tag = music.title
            itemView.setOnClickListener {
                mListener.onPlayingItemClick(position)
            }
//            delete.click
        }
    }

    override fun getItemCount(): Int {
        return listBean.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        fun onPlayingItemClick(position: Int)

//        fun onDeleteItemClick(position: Int)
    }

    fun addData(newItem:Music) {
        listBean.add(newItem)
        notifyItemInserted(listBean.size - 1)
    }

    fun removeData(position: Int) {
        listBean.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateData(position: Int) {
        nowPlaying = position
        notifyItemChanged(position)
    }

    fun setNowPlaying(position: Int) {
        nowPlaying = position
    }

}