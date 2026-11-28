package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PreferenceKeysTest {
    @Test
    fun preferenceFilesAndKeysAreCentralized() {
        assertEquals("app_data", PreferenceKeys.Files.APP_DATA)
        assertEquals("desktop_mode_prefs", PreferenceKeys.Files.DESKTOP_MODE)
        assertEquals("theme_prefs", PreferenceKeys.Files.THEME)
        assertEquals("immersive_mode_prefs", PreferenceKeys.Files.IMMERSIVE_MODE)
        assertEquals("desktop_mode", PreferenceKeys.Keys.DESKTOP_MODE)
        assertEquals("theme_mode", PreferenceKeys.Keys.THEME_MODE)
        assertEquals("immersive_mode", PreferenceKeys.Keys.IMMERSIVE_MODE)
    }
}
