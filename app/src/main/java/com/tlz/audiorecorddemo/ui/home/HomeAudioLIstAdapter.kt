package com.tlz.audiorecorddemo.ui.home

import android.annotation.SuppressLint
import android.graphics.drawable.LevelListDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tlz.audiorecorddemo.R
import com.tlz.audiorecorddemo.core.AudioPlayer
import com.tlz.audiorecorddemo.model.AudioFileItem
import kotlinx.android.synthetic.main.item_audio.view.*
import java.io.File

/**
 * Created by Tomlezen.
 * Date: 2018/12/21.
 * Time: 9:52 PM.
 */
class HomeAudioLIstAdapter : RecyclerView.Adapter<HomeAudioLIstAdapter.ItemViewHolder>(),
    AudioPlayer.AudioPlayListener {

    val data = mutableListOf<AudioFileItem>()

    /** 准备播放的位置. */
    private var preparePosition = -1
    /** 当前正在播放的位置. */
    private var playingPosition = -1
    private var isShowProgressBar = false

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemViewHolder =
        ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.itemView.apply {
            val item = data[position]
            tv_file_name.text = item.name
            val isPlaying = playingPosition == position && isShowProgressBar
            pb_playing.visibility = if (isPlaying) View.VISIBLE else View.GONE

            (iv_play.drawable as? LevelListDrawable)?.level = if (isPlaying) 1 else 0
            iv_play.setOnClickListener {
                when ((iv_play.drawable as? LevelListDrawable)?.level) {
                    0 -> {
                        preparePosition = holder.adapterPosition
                        AudioPlayer.play(item.path, this@HomeAudioLIstAdapter)
                    }
                    1 -> {
                        AudioPlayer.stop()
                    }
                }
            }

            iv_delete.setOnClickListener {
                kotlin.runCatching {
                    val pos = holder.adapterPosition
                    if (playingPosition == pos) {
                        AudioPlayer.stop()
                    }
                    File(item.path).delete()
                    data.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        }
    }

    fun setNewData(data: List<AudioFileItem>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: AudioFileItem) {
        this.data.add(0, data)
        notifyItemInserted(0)
    }

    override fun onPlayPreparing() {
        playingPosition = this.preparePosition
        if (playingPosition != -1) {
            isShowProgressBar = true
            notifyItemChanged(playingPosition)
        }
    }

    override fun onPlayStart() {
        playingPosition = this.preparePosition
        if (playingPosition != -1) {
            isShowProgressBar = true
            notifyItemChanged(playingPosition)
        }
    }

    override fun onPlayStop() {
        if (playingPosition != -1) {
            isShowProgressBar = false
            notifyItemChanged(playingPosition)
        }
    }

    override fun onPlayError(t: Throwable) {
        if (playingPosition != -1) {
            isShowProgressBar = false
            notifyItemChanged(playingPosition)
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
}