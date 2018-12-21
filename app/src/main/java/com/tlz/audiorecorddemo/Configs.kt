package com.tlz.audiorecorddemo

import android.os.Environment

/**
 * By tomlezen
 * Create at 2018/12/21
 */
object Configs {
    /** 音频文件保存目录. */
    val AUDIO_SAVE_FLODER = Environment.getExternalStorageDirectory().absolutePath + "/audios"
}