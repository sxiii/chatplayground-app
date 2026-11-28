package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Rapid lifecycle tests for [NetworkMonitor] to ensure no registration leaks.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorRapidLifecycleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cm: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun registeredCallbacks() = shadowOf(cm).networkCallbacks

    @Before
    fun clearRegisteredCallbacks() {
        registeredCallbacks().clear()
    }

    @Test
    fun rapidObserveStopAlternationLeavesZeroCallbacks() {
        val monitor = NetworkMonitor(context)

        repeat(100) {
            monitor.observe { }
            monitor.stopObserving()
        }

        assertTrue(
            "After rapid observe/stop alternation, no callbacks should remain registered",
            registeredCallbacks().isEmpty(),
        )
    }

    @Test
    fun rapidObserveCallsLeaveExactlyOneCallback() {
        val monitor = NetworkMonitor(context)

        repeat(100) {
            monitor.observe { }
        }

        assertEquals(
            "After rapid observe calls, exactly one callback should remain registered",
            1,
            registeredCallbacks().size,
        )

        monitor.stopObserving()
        assertTrue(registeredCallbacks().isEmpty())
    }
}
