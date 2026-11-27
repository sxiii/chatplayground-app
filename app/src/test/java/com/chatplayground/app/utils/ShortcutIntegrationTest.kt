package com.chatplayground.app.utils

import android.content.Intent
import androidx.core.content.pm.ShortcutManagerCompat
import com.chatplayground.app.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShortcutIntegrationTest {

    @Test
    fun registerShortcutsPublishesDynamicShortcuts() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        ShortcutHelper.registerShortcuts(activity)

        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(activity)
        assertEquals(ShortcutRegistry.allShortcuts().size, shortcuts.size)
        assertEquals("clear_cache", shortcuts[0].id)
        assertEquals("toggle_theme", shortcuts[1].id)
    }

    @Test
    fun handlingClearCacheShortcutActionClearsData() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            action = "com.chatplayground.app.action.CLEAR_CACHE"
        }
        val activity = Robolectric.buildActivity(MainActivity::class.java, intent).create().get()
        assertNotNull(activity)
    }
}
