package com.chatplayground.app.utils

import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import com.chatplayground.app.MainActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImmersiveModeWindowAdapterInstrumentedTest {
    @Test
    fun applyImmersiveModeHidesAndRestoresSystemBarsOnRealActivityWindow() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val window = activity.window
                ImmersiveModeWindowAdapter.apply(window, true)
                assertSystemBarsVisible(window, expected = false)

                ImmersiveModeWindowAdapter.apply(window, false)
                assertSystemBarsVisible(window, expected = true)
            }
        }
    }

    private fun assertSystemBarsVisible(
        window: Window,
        expected: Boolean,
    ) {
        val insets = ViewCompat.getRootWindowInsets(window.decorView)
        assertTrue("The test activity must expose root window insets", insets != null)
        assertEqualsVisibility(
            expected,
            insets!!.isVisible(WindowInsetsCompat.Type.systemBars()),
        )
    }

    private fun assertEqualsVisibility(
        expected: Boolean,
        actual: Boolean,
    ) {
        if (expected) {
            assertTrue("System bars should be visible", actual)
        } else {
            assertFalse("System bars should be hidden", actual)
        }
    }
}
