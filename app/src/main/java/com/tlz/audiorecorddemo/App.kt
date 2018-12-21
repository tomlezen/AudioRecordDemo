package com.tlz.audiorecorddemo

import android.app.Application
import com.tlz.fuckpermission.FuckPermissionProcessor

/**
 * Created by Tomlezen.
 * Date: 2018/12/21.
 * Time: 10:06 PM.
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        FuckPermissionProcessor().install(this)
    }

}