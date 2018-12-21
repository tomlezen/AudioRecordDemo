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
import com.tlz.audiorecorddemo.core.AudioRecordListener
import com.tlz.audiorecorddemo.core.AudioRecorder
import com.tlz.audiorecorddemo.extensions.beginDelayedTransition
import com.tlz.audiorecorddemo.extensions.firbidScroll
import kotlinx.android.synthetic.main.dialog_fragment_audio_record.*
import java.io.File

/**
 * By tomlezen
 * Create at 2018/12/21
 */
class AudioRecordDialogFragment : BottomSheetDialogFragment(), AudioRecordListener {

    /** 录制结果. */
    private var recordResult: ((File) -> Unit)? = null

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
                0 -> AudioRecorder.start()
                1 -> AudioRecorder.stop()
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
        (iv_action.drawable as? LevelListDrawable)?.level = 1
        pb_record?.visibility = View.VISIBLE
        Toast.makeText(context, "开始录制", Toast.LENGTH_LONG).show()
    }

    override fun onRecordError(t: Throwable) {
        Toast.makeText(context, "录制失败", Toast.LENGTH_LONG).show()
        (iv_action?.parent as? ViewGroup)?.beginDelayedTransition()
        iv_action?.visibility = View.VISIBLE
        (iv_action?.drawable as? LevelListDrawable)?.level = 0
        iv_delete?.visibility = View.GONE
        iv_done?.visibility = View.GONE
        pb_record?.visibility = View.GONE
    }

    override fun onRecordStop() {
        (iv_action?.parent as? ViewGroup)?.beginDelayedTransition()
        iv_action?.visibility = View.INVISIBLE
        iv_delete?.visibility = View.VISIBLE
        iv_done?.visibility = View.VISIBLE
        pb_record?.visibility = View.GONE
    }

    override fun onRecordCancel() {
        (iv_action?.parent as? ViewGroup)?.beginDelayedTransition()
        iv_action?.visibility = View.VISIBLE
        pb_record?.visibility = View.GONE
        iv_delete?.visibility = View.GONE
        iv_done?.visibility = View.GONE
    }

    override fun onRecordComplete(audioFile: File) {
        recordResult?.invoke(audioFile)
        dismiss()
    }

    override fun onDestroy() {
        AudioRecorder.cancel()
        AudioRecorder.recordListener = null
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