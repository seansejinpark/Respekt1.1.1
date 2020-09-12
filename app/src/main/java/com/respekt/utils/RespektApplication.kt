package com.respekt.utils

import android.app.Application

class RespektApplication : Application() {

    companion object {
        var instance: RespektApplication? = null

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}