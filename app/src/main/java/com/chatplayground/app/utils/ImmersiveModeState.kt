package com.chatplayground.app.utils

import android.content.SharedPreferences

/** Pure state and persistence rules for the immersive-mode setting. */
object ImmersiveModeState {
    private const val KEY_IMMERSIVE_MODE = "immersive_mode"

    var isImmersive: Boolean = false
        private set

    fun load(preferences: SharedPreferences) {
        isImmersive = preferences.getBoolean(KEY_IMMERSIVE_MODE, false)
    }

    fun toggle(preferences: SharedPreferences? = null): Boolean {
        isImmersive = !isImmersive
        preferences?.edit()?.putBoolean(KEY_IMMERSIVE_MODE, isImmersive)?.apply()
        return isImmersive
    }

    fun applyTo(applier: (Boolean) -> Unit) {
        applier(isImmersive)
    }

    fun resetForTesting() {
        isImmersive = false
    }
}
