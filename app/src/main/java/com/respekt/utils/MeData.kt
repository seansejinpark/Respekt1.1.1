package com.respekt.utils

class MeData {

    companion object {
        val Key_TotalSessions = "TotalSessions"
        val Key_TotalMinutes = "TotalMinutes"
        val Key_ResentSession = "ResentSession"
        val Key_PurchasedState = "PurchasedState"

        val NotPurchased = "0"
        val Purchased = "1"
        val Expire = "2"

        fun getTotalSessions(): String? {
            return Preferences.instance?.get(Key_TotalSessions, String::class.java, "0")
        }

        fun setTotalSessions(session: String) {
            Preferences.instance?.put(Key_TotalSessions, session)
        }

        fun getTotalMinutes(): String? {
            return Preferences.instance?.get(Key_TotalMinutes, String::class.java, "0")
        }

        fun setTotalMinutes(minutes: String) {
            Preferences.instance?.put(Key_TotalMinutes, minutes)
        }

        fun getResentSession(): LastSession? {
            return Preferences.instance?.get(
                Key_ResentSession,
                LastSession::class.java, LastSession(mutableListOf())
            )
        }

        fun setResentSession(session: LastSession) {
            Preferences.instance?.put(Key_ResentSession, session)
        }

        fun getPurchasedState(): String? {
            return Preferences.instance?.get(Key_PurchasedState, String::class.java, "0")
        }

        fun setPurchasedState(minutes: String) {
            Preferences.instance?.put(Key_PurchasedState, minutes)
        }
    }

}