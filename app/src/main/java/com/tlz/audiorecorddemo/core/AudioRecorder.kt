package com.tlz.audiorecorddemo.core

import android.media.MediaRecorder
import android.util.Log
import com.tlz.audiorecorddemo.Configs
import java.io.File

/**
 * By tomlezen
 * Create at 2018/12/21
 */
interface AudioRecorder {

    /** 录制状态. */
    val recordStatus: RecordStatus

    /** 录制监听. */
    var recordListener: AudioRecordListener?

    /**
     * 开始录制.
     */
    fun start()

    /**
     * 停止录制.
     */
    fun stop()

    /**
     * 取消录制.
     */
    fun cancel()

    /**
     * 保存录制文件.
     */
    fun done()

    /**
     * 释放.
     */
    fun release()

    companion object : AudioRecorder {
        internal const val TAG = "AudioRecorder"

        private var sInstance: AudioRecorder? = null

        operator fun invoke(): AudioRecorder =
            sInstance ?: AudioRecorderImpl().also { sInstance = it }

        override val recordStatus: RecordStatus
            get() = AudioRecorder().recordStatus

        override var recordListener: AudioRecordListener?
            get() = AudioRecorder().recordListener
            set(value) {
                AudioRecorder().recordListener = value
            }

        override fun start() {
            AudioRecorder().start()
        }

        override fun stop() {
            AudioRecorder().stop()
        }

        override fun cancel() {
            AudioRecorder().cancel()
        }

        override fun done() {
            AudioRecorder().done()
        }

        override fun release() {
            AudioRecorder().release()
        }
    }

    /**
     * 音频录制监听.
     */
    interface AudioRecordListener {
        /**
         * 录制开始.
         */
        fun onRecordStart()

        /**
         * 录制完成.
         * @param audioFileCachePath String
         */
        fun onRecordComplete(audioFileCachePath: String)

        /**
         * 录制取消.
         */
        fun onRecordCancel()

        /**
         * 录制保存完成.
         * @param audioFile File
         */
        fun onRecordSaved(audioFile: File)

        /**
         * 录制错误.
         * @param t Throwable
         */
        fun onRecordError(t: Throwable)
    }

    enum class RecordStatus {
        IDLE, PREPARING, RECORDING, RECORD_COMPLETE, SAVING
    }

}

private class AudioRecorderImpl : AudioRecorder {

    private val mediaRecorder by lazy {
        MediaRecorder().apply {
            setOnInfoListener { _, what, extra ->
                Log.i(AudioRecorder.TAG, "what = $what, extra = $extra")
                when (what) {
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                        this@AudioRecorderImpl.stop()
                    }
                }
            }
            setOnErrorListener { _, what, extra ->
                _recordStatus = AudioRecorder.RecordStatus.IDLE
                val errorMsg = "what = $what, extra = $extra"
                _recordListener?.onRecordError(Exception("errorMsg"))
                Log.e(AudioRecorder.TAG, errorMsg)
            }
        }
    }

    /** 录制状态. */
    private var _recordStatus = AudioRecorder.RecordStatus.IDLE

    override val recordStatus: AudioRecorder.RecordStatus
        get() = _recordStatus

    /** 录制监听. */
    private var _recordListener: AudioRecorder.AudioRecordListener? = null

    override var recordListener: AudioRecorder.AudioRecordListener?
        get() = _recordListener
        set(value) {
            _recordListener = value
        }

    /** 创建音频保存文件. */
    private val audioFile: File
        get() = File(Configs.AUDIO_SAVE_FLODER, "${System.currentTimeMillis()}.m4a").create()

    /** 创建音频缓存文件 */
    private val audioCacheFile: File = File(Configs.AUDIO_SAVE_FLODER, "audio_record_demo.cache").create()

    override fun start() {
        if (_recordStatus != AudioRecorder.RecordStatus.IDLE) return
        runCatching {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            // 最大1分钟
            mediaRecorder.setMaxDuration(60_000)
            mediaRecorder.setOutputFile(audioCacheFile.absolutePath )
            _recordStatus = AudioRecorder.RecordStatus.PREPARING
            mediaRecorder.prepare()
            mediaRecorder.start()
            _recordStatus = AudioRecorder.RecordStatus.RECORDING
            _recordListener?.onRecordStart()
        }.onFailure {
            Log.e(AudioRecorder.TAG, "音频录制失败", it)
            _recordStatus = AudioRecorder.RecordStatus.IDLE
            _recordListener?.onRecordError(it)
        }
    }

    override fun stop() {
        if (_recordStatus != AudioRecorder.RecordStatus.RECORDING) return
        mediaRecorder.stop()
        _recordStatus = AudioRecorder.RecordStatus.RECORD_COMPLETE
        _recordListener?.onRecordComplete(audioCacheFile.absolutePath)
    }

    override fun cancel() {
        mediaRecorder.reset()
        audioCacheFile.delete()
        _recordStatus = AudioRecorder.RecordStatus.IDLE
        _recordListener?.onRecordCancel()
    }

    override fun done() {
        if (_recordStatus != AudioRecorder.RecordStatus.RECORD_COMPLETE && audioCacheFile.exists()) return
        kotlin.runCatching {
            // 这里应该异步保存 偷个懒
            _recordListener?.onRecordSaved(audioCacheFile.copyTo(this.audioFile, true))
        }.onFailure {
            _recordListener?.onRecordError(it)
        }
        _recordStatus = AudioRecorder.RecordStatus.IDLE
    }

    override fun release() {
        _recordStatus = AudioRecorder.RecordStatus.IDLE
        mediaRecorder.release()
    }

    /**
     * 创建文件.
     * @receiver File
     * @return File
     */
    private fun File.create(): File {
        if (!File(Configs.AUDIO_SAVE_FLODER).exists()) {
            mkdirs()
        }
        if (exists()) {
            delete()
        }
        createNewFile()
        return this
    }

}