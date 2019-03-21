package com.branamplayer.android.app

import android.app.Application
import android.content.Context
import com.google.android.gms.common.wrappers.InstantApps
import com.google.android.play.core.splitcompat.SplitCompat

@Suppress("Unused")
class App : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

//        if (!InstantApps.isInstantApp(this)) {
            SplitCompat.install(this)
//        }
    }
}
