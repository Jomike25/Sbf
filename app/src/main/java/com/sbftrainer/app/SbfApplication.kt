package com.sbftrainer.app

import android.app.Application

class SbfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ProgressStore.init(this)
    }
}
