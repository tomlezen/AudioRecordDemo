package com.tlz.audiorecorddemo.core

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/**
 * By tomlezen
 * Create at 2018/12/21
 */
interface AudioPlayer {

    /**
     * 播放.
     * @param audioPath String 音频文件路径
     * @param playListener AudioPlayListener 播放监听.
     */
    fun play(audioPath: String, playListener: AudioPlayListener)

    /**
     * 停止播放.
     */
    fun stop()

    /**
     * 取消播放.
     */
    fun cancel()

    /**
     * 释放.
     */
    fun release()

    companion object : AudioPlayer {
        internal const val TAG = "AudioPlayer"
        private var sInstance: AudioPlayer? = null

        operator fun invoke(): AudioPlayer =
            sInstance ?: AudioPlayerImpl().also { sInstance = it }

        override fun play(audioPath: String, playListener: AudioPlayListener) {
            AudioPlayer().play(audioPath, playListener)
        }

        override fun stop() {
            AudioPlayer().stop()
        }

        override fun cancel() {
            AudioPlayer().cancel()
        }

        override fun release() {
            AudioPlayer().release()
        }
    }

    /**
     * 音频播放监听.
     */
    interface AudioPlayListener {
        /**
         * 播放准备中.
         */
        fun onPlayPreparing()

        /**
         * 播放开始.
         */
        fun onPlayStart()

        /**
         * 播放停止.
         */
        fun onPlayStop()

        /**
         * 播放异常.
         * @param t Throwable
         */
        fun onPlayError(t: Throwable)
    }
}

private class AudioPlayerImpl : AudioPlayer {

    /** 是否已经准备完成. */
    private var isPrepared = false

    /** 是否正在准备中. */
    private var isPreparing = false
        set(value) {
            field = value
            if (value) {
                playListener?.onPlayPreparing()
            }
        }

    /** 播放器. */
    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            setOnPreparedListener {
                isPreparing = false
                isPrepared = true
                start()
                playListener?.onPlayStart()
            }
            // 不监听错误即会回调完成方法
            setOnCompletionListener {
                isPreparing = false
                playListener?.onPlayStop()
            }
        }
    }

    /** 当前正在播放的音频路径. */
    private var playingAudioPath: String? = null
    /** 播放监听. */
    private var playListener: AudioPlayer.AudioPlayListener? = null

    override fun play(audioPath: String, playListener: AudioPlayer.AudioPlayListener) {
        if (audioPath != playingAudioPath || (!isPreparing && !mediaPlayer.isPlaying)) {
            // 先停掉之前播放的音频
            if (mediaPlayer.isPlaying) {
                stop()
            }
            mediaPlayer.reset()
            playingAudioPath = audioPath
            this.playListener = playListener
            mediaPlayer.setDataSource(audioPath)
            try {
                isPreparing = true
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                isPreparing = false
                Log.e(AudioPlayer.TAG, "播放失败", e)
                playListener.onPlayError(e)
            }
        }
    }

    override fun stop() {
        mediaPlayer.stop()
        isPreparing = false
        playListener?.onPlayStop()
    }

    override fun cancel() {
        mediaPlayer.stop()
        isPreparing = false
        playingAudioPath = null
        playListener = null
    }

    override fun release() {
        cancel()
        mediaPlayer.release()
    }

}