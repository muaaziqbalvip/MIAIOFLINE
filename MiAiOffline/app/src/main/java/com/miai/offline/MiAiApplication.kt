package com.miai.offline

import android.app.Application

class MiAiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Future: initialize crash reporting, model cache warmup, etc.
    }
}
