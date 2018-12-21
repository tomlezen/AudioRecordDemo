package com.tlz.audiorecorddemo.ui.record

import android.graphics.drawable.LevelListDrawable
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tlz.audiorecorddemo.R
import com.tlz.audiorecorddemo.core.AudioPlayer
import com.tlz.audiorecorddemo.core.AudioRecorder
import com.tlz.audiorecorddemo.extensions.beginDelayedTransition
import com.tlz.audiorecorddemo.extensions.firbidScroll
import kotlinx.android.synthetic.main.dialog_fragment_audio_record.*
import java.io.File

/**
 * By tomlezen
 * Create at 2018/12/21
 */
class AudioRecordDialogFragment : BottomSheetDialogFragment(), AudioRecorder.AudioRecordListener,
    AudioPlayer.AudioPlayListener {

    /** 录制结果. */
    private var recordResult: ((File) -> Unit)? = null

    /** 录制缓存文件路径. */
    private var audioFileCachePath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_audio_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
    }

    private fun initView() {
        isCancelable = false
        firbidScroll()
        iv_delete.setOnClickListener {
            AudioRecorder.cancel()
        }

        iv_action.setOnClickListener {
            when ((iv_action.drawable as? LevelListDrawable)?.level) {
                0 -> {
                    if (AudioRecorder.recordStatus == AudioRecorder.RecordStatus.RECORD_COMPLETE) {
                        AudioPlayer.play(audioFileCachePath ?: return@setOnClickListener, this)
                    } else {
                        AudioRecorder.start()
                    }
                }
                1 -> {
                    if (AudioRecorder.recordStatus == AudioRecorder.RecordStatus.RECORD_COMPLETE) {
                        AudioPlayer.stop()
                    } else {
                        AudioRecorder.stop()
                    }
                }
            }
        }

        iv_done.setOnClickListener {
            AudioRecorder.done()
        }

        iv_close.setOnClickListener {
            AudioRecorder.cancel()
            dismiss()
        }
    }

    private fun initData() {
        AudioRecorder.recordListener = this
    }

    override fun onRecordStart() {
        setPlayMode()
        iv_delete?.visibility = View.GONE
        iv_done?.visibility = View.GONE
        Toast.makeText(context, "开始录制", Toast.LENGTH_LONG).show()
    }

    override fun onRecordError(t: Throwable) {
        Toast.makeText(context, "录制失败", Toast.LENGTH_LONG).show()
        (iv_action?.parent as? ViewGroup)?.beginDelayedTransition()
        iv_action?.visibility = View.VISIBLE
        iv_delete?.visibility = View.GONE
        iv_done?.visibility = View.GONE
        setStopMode()
    }

    override fun onRecordComplete(audioFileCachePath: String) {
        this.audioFileCachePath = audioFileCachePath
        (iv_action?.parent as? ViewGroup)?.beginDelayedTransition()
        iv_delete?.visibility = View.VISIBLE
        iv_done?.visibility = View.VISIBLE
        setStopMode()
    }

    override fun onRecordCancel() {
        (iv_action?.parent as? ViewGroup)?.beginDelayedTransition()
        iv_action?.visibility = View.VISIBLE
        setStopMode()
        iv_delete?.visibility = View.GONE
        iv_done?.visibility = View.GONE
        AudioPlayer.stop()
    }

    override fun onRecordSaved(audioFile: File) {
        recordResult?.invoke(audioFile)
        dismiss()
    }

    override fun onPlayPreparing() {
        setPlayMode()
    }

    override fun onPlayStart() {
        setPlayMode()
    }

    override fun onPlayStop() {
        setStopMode()
    }

    override fun onPlayError(t: Throwable) {
        setStopMode()
        Toast.makeText(context, "播放失败", Toast.LENGTH_LONG).show()
    }

    private fun setPlayMode() {
        (iv_action?.drawable as? LevelListDrawable)?.level = 1
        pb_record?.visibility = View.VISIBLE
    }

    private fun setStopMode() {
        (iv_action?.drawable as? LevelListDrawable)?.level = 0
        pb_record?.visibility = View.GONE
    }

    override fun onDestroy() {
        AudioRecorder.cancel()
        AudioRecorder.recordListener = null
        AudioPlayer.cancel()
        super.onDestroy()
    }

    companion object {
        /**
         * 显示录制框.
         * @param act FragmentActivity
         * @param recordResult (File) -> Unit
         * @return AudioRecordDialogFragment
         */
        fun show(act: FragmentActivity, recordResult: (File) -> Unit) =
            AudioRecordDialogFragment().apply {
                this.recordResult = recordResult
                show(act.supportFragmentManager, AudioRecordDialogFragment::class.java.canonicalName)
            }
    }

}