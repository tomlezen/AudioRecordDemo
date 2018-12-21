package com.tlz.audiorecorddemo.ui.home

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.tlz.audiorecorddemo.R
import com.tlz.audiorecorddemo.core.AudioPlayer
import com.tlz.audiorecorddemo.core.AudioRecorder
import com.tlz.audiorecorddemo.extensions.dp2px
import com.tlz.audiorecorddemo.model.AudioFileItem
import com.tlz.audiorecorddemo.ui.record.AudioRecordDialogFragment
import com.tlz.fuckpermission.FuckPermissionCallback
import com.tlz.fuckpermission.FuckPermissionOperate
import com.tlz.fuckpermission.annotations.FuckPermission
import kotlinx.android.synthetic.main.activity_home.*

@FuckPermission(permissions = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO])
class HomeActivity : AppCompatActivity(), FuckPermissionCallback {

    private val viewModel by lazy { ViewModelProviders.of(this).get(HomeViewModel::class.java) }

    /** 适配器. */
    private val adapter by lazy { HomeAudioLIstAdapter() }

    /** 权限是否被运行. */
    private var isPermissionOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            R.id.menu_action_add -> {
                if (!isPermissionOk) {
                    Toast.makeText(this, "缺少权限", Toast.LENGTH_LONG).show()
                } else {
                    // 录音前停止播放音频
                    AudioPlayer.stop()
                    // 显示录制框
                    AudioRecordDialogFragment.show(this) {
                        adapter.addData(AudioFileItem(it.name, it.absolutePath))
                        rv_audio_list.smoothScrollToPosition(0)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onFuckPermissionRevoked(
        operate: FuckPermissionOperate,
        grantedPermissions: Array<String>,
        revokedPermissions: Array<String>,
        canShowRequestPermissionRationale: Boolean
    ) {
        if (canShowRequestPermissionRationale) {
            operate.requestPermission()
        }
        swipe_refresh_layout.isEnabled = false
    }

    override fun onFuckPermissionGranted(operate: FuckPermissionOperate) {
        isPermissionOk = true
        swipe_refresh_layout.isEnabled = true
        viewModel.loadAudioList()
    }

    override fun onDestroy() {
        AudioRecorder.release()
        AudioPlayer.release()
        super.onDestroy()
    }
}
