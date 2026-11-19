package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetworkInfo

/**
 * Simulates network state transitions through Robolectric's
 * [ShadowConnectivityManager] to verify [NetworkMonitor.observe] delivers
 * onAvailable / onLost to its registered callback and flips [isConnected].
 *
 * Robolectric's default device has no active network and does not replay
 * ConnectivityManager callbacks, so these transitions must be driven
 * explicitly (see .autonomy/vision.md).
 */
@RunWith(RobolectricTestRunner::class)
@Config(minSdk = Build.VERSION_CODES.N)
@Suppress("DEPRECATION")
class NetworkMonitorObserveTest {

    private lateinit var monitor: NetworkMonitor
    private lateinit var shadowCm: ShadowConnectivityManager

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        shadowCm = shadowOf(cm)
        monitor = NetworkMonitor(context)
    }

    @Test
    fun observeReportsOnlineThenOfflineOnNetworkTransitions() {
        val events = mutableListOf<Boolean>()
        monitor.observe { events.add(it) }

        // A mobile network comes online.
        val info = ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_MOBILE,
            TelephonyManager.NETWORK_TYPE_LTE,
            true,
            NetworkInfo.State.CONNECTED
        )
        shadowCm.setActiveNetworkInfo(info)
        shadowCm.setDefaultNetworkActive(true)

        assertTrue("monitor should be online once the default network is available", monitor.isConnected)
        assertEquals(listOf(true), events)

        // The network goes away.
        shadowCm.setDefaultNetworkActive(false)

        assertFalse("monitor should be offline once the default network is lost", monitor.isConnected)
        assertEquals(listOf(true, false), events)
    }

    @Test
    fun observeReplacesPreviousCallbackOnRepeatCalls() {
        val firstEvents = mutableListOf<Boolean>()
        val secondEvents = mutableListOf<Boolean>()

        monitor.observe { firstEvents.add(it) }
        monitor.observe { secondEvents.add(it) }

        val info = ShadowNetworkInfo.newInstance(
            null,
            ConnectivityManager.TYPE_WIFI,
            TelephonyManager.NETWORK_TYPE_LTE,
            true,
            NetworkInfo.State.CONNECTED
        )
        shadowCm.setActiveNetworkInfo(info)
        shadowCm.setDefaultNetworkActive(true)

        // Only the most recently registered callback should be notified
        // (observe unregisters the previous callback before registering).
        assertEquals(emptyList(), firstEvents)
        assertEquals(listOf(true), secondEvents)
    }
}
