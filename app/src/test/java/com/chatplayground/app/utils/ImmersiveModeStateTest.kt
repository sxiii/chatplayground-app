package com.chatplayground.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImmersiveModeStateTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences = context.getSharedPreferences("immersive_mode_prefs", Context.MODE_PRIVATE)

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
        ImmersiveModeState.resetForTesting()
    }

    @Test
    fun togglePersistsImmersiveModeAcrossStateReload() {
        ImmersiveModeState.load(preferences)

        assertFalse(ImmersiveModeState.isImmersive)
        assertTrue(ImmersiveModeState.toggle(preferences))

        ImmersiveModeState.resetForTesting()
        ImmersiveModeState.load(preferences)

        assertTrue(ImmersiveModeState.isImmersive)
    }

    @Test
    fun toggleOffPersistsDisabledState() {
        preferences.edit().putBoolean("immersive_mode", true).commit()
        ImmersiveModeState.load(preferences)

        assertFalse(ImmersiveModeState.toggle(preferences))

        ImmersiveModeState.resetForTesting()
        ImmersiveModeState.load(preferences)

        assertFalse(ImmersiveModeState.isImmersive)
    }
}
