package com.branhamplayer.android.reactivesplitinstaller.app

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat

@Suppress("Unused")
class App : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }
}
