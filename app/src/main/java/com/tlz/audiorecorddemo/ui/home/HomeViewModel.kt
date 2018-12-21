package com.tlz.audiorecorddemo.ui.home

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.tlz.audiorecorddemo.Configs
import com.tlz.audiorecorddemo.model.AudioFileItem
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * By tomlezen
 * Create at 2018/12/21
 */
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    /** 加载状态. */
    private val _loadStatus = MutableLiveData<Boolean>()
    val loadStatus: LiveData<Boolean>
        get() = _loadStatus

    /** 音频列表. */
    private val _data = MutableLiveData<List<AudioFileItem>>()
    val data: LiveData<List<AudioFileItem>>
        get() = _data

    private val disposable = CompositeDisposable()

    /**
     * 加载音频列表.
     */
    fun loadAudioList() {
        if (_loadStatus.value == true) return
        _loadStatus.value = true
        disposable.add(
            Single.create<List<AudioFileItem>> {
                val folder = File(Configs.AUDIO_SAVE_FLODER)
                val data = mutableListOf<AudioFileItem>()
                if (folder.exists()) {
                    folder.listFiles()
                        .filter { f -> f.extension == "m4a" }
                        .map { f -> AudioFileItem(f.name, f.absolutePath) }
                        .mapTo(data) { f -> f }
                }
                it.onSuccess(data)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {
                    _loadStatus.value = false
                }.subscribe({
                    _data.value = it
                }) {
                    Log.e(TAG, "音频列表加载失败")
                }
        )
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    companion object {
        private val TAG = HomeViewModel::class.java.canonicalName
    }

}