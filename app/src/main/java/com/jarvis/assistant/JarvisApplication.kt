package com.jarvis.assistant

import android.app.Application

class JarvisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Reserved for lightweight app-wide init (e.g. crash reporting opt-in).
        // Intentionally does not eagerly request any runtime permission here.
    }
}
