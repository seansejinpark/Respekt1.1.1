package com.respekt.utils

import com.github.pwittchen.prefser.library.rx2.Prefser

/**
 * The type Preferences.
 */
object Preferences {
    var Preferences: Prefser? = null

    val instance: Prefser?
        get() {
            if (Preferences == null) {
                Preferences =
                    RespektApplication.instance?.let { Prefser(it) }
            }
            return Preferences
        }
}