package com.chatplayground.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behavioural tests for [NetworkMonitor].
 *
 * These deliberately avoid shadow network internals: Robolectric's default
 * state has no active network, and `ConnectivityManager` callbacks are not
 * replayed by `ShadowConnectivityManager`. Tests that need real callback
 * simulation belong in a later iteration (see .autonomy/vision.md).
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorTest {

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun reportsOfflineWhenNoNetworkIsAvailable() {
        val monitor = NetworkMonitor(context())

        assertFalse(
            "A Robolectric device with no active network should be reported offline",
            monitor.checkConnectivity()
        )
    }

    @Test
    fun isConnectedMirrorsCheckConnectivity() {
        val monitor = NetworkMonitor(context())

        assertEquals(monitor.checkConnectivity(), monitor.isConnected)
    }

    @Test
    fun observeThenStopObservingIsIdempotent() {
        val monitor = NetworkMonitor(context())

        monitor.observe { }
        monitor.stopObserving()
        monitor.stopObserving()
    }
}
