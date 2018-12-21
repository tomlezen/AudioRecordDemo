package com.tlz.audiorecorddemo.ui.home

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.tlz.audiorecorddemo.R
import com.tlz.audiorecorddemo.core.AudioRecorder
import com.tlz.audiorecorddemo.extensions.dp2px
import com.tlz.audiorecorddemo.model.AudioFileItem
import com.tlz.audiorecorddemo.ui.record.AudioRecordDialogFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_audio.view.*
import java.io.File


class HomeActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(HomeViewModel::class.java) }

    /** 适配器. */
    private val adapter by lazy { AudioListAdapter(::onAudioPlay) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE)
        }

        initView()
        initData()
    }

    private fun initView() {
        swipe_refresh_layout.setOnRefreshListener { viewModel.loadAudioList() }

        rv_audio_list.adapter = adapter
        rv_audio_list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val dividerHeight = dp2px(1)
            private val dividerDrawable = ColorDrawable(Color.LTGRAY)

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                (0 until parent.childCount)
                    .map { parent.getChildAt(it) }
                    .forEach {
                        it?.run {
                            dividerDrawable.setBounds(left, bottom, right, bottom + dividerHeight)
                            dividerDrawable.draw(c)
                        }
                    }
            }

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, dividerHeight, 0)
            }
        })
    }

    private fun initData() {
        viewModel.apply {
            loadStatus.observe(this@HomeActivity, Observer {
                swipe_refresh_layout.isRefreshing = it == true
            })

            data.observe(this@HomeActivity, Observer {
                adapter.setNewData(it ?: listOf())
            })

            loadAudioList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            R.id.menu_action_add -> {
                // 显示录制框
                AudioRecordDialogFragment.show(this) { adapter.addData(AudioFileItem(it.name, it.absolutePath)) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onDestroy() {
        AudioRecorder.release()
        super.onDestroy()
    }

    private fun onAudioPlay(item: AudioFileItem, position: Int) {

    }

    private class AudioListAdapter(private val onPlay: (AudioFileItem, Int) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

        val data = mutableListOf<AudioFileItem>()

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.itemView.apply {
                val item = data[position]
                tv_file_name.text = item.name

                iv_play.setOnClickListener { onPlay.invoke(item, position) }

                iv_delete.setOnClickListener {
                    kotlin.runCatching {
                        File(item.path).delete()
                        data.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
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
    }

    private class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100001
        private val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
