package com.m2mmusic.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.m2mmusic.android.databinding.ItemMusicListBinding
import com.m2mmusic.android.logic.model.Music

class MusicListAdapter(private val listBean: List<Music>) :
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    inner class ViewHolder(binding: ItemMusicListBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemCount: TextView = binding.count
        val title: TextView = binding.musicName
        val artists: TextView = binding.musicArtist
        val alia: TextView = binding.alia
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
            itemCount.text = (position + 1).toString()
            title.text = music.title
//            alia.text = music.alia
            alia.text = ""
            artists.text = music.artist
            itemView.tag = music.title
            itemView.setOnClickListener {
                mListener.onMusicListItemClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return listBean.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        fun onMusicListItemClick(position: Int)
    }

}