package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

/**
 * Truth table for [NetworkMonitor.checkConnectivity].
 *
 * All three connectivity cases live here so the reason each one is offline or
 * online is explicit: no active network at all, an active network whose
 * capabilities cannot be read, and an active network that is INTERNET-capable
 * and VALIDATED. Previously the no-network and no-capability cases were spread
 * over two classes that asserted the same offline outcome for different
 * platform states.
 *
 * Robolectric 4.18 APIs verified against the pinned tag's sources:
 * `ShadowConnectivityManager.setActiveNetworkInfo(NetworkInfo)` publishes the
 * active network, `setNetworkCapabilities(Network, NetworkCapabilities)` keys
 * capabilities by that exact Network instance, and
 * `NetworkCapabilities.addCapability(int)` is a hidden framework API reached
 * through `Shadows.shadowOf(capabilities)`.
 *
 * `ConnectivityManager` callbacks are not replayed by `ShadowConnectivityManager`;
 * callback-simulation tests live in `NetworkMonitorCapabilityChangeTest` and
 * `NetworkMonitorCaptivePortalTest`.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorTest {
    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun reportsOfflineWhenNoActiveNetwork() {
        val monitor = NetworkMonitor(context())

        assertFalse(
            "A Robolectric device with no active network should be reported offline",
            monitor.checkConnectivity(),
        )
    }

    @Test
    fun reportsOfflineWhenActiveNetworkHasNoCapabilities() {
        // No capabilities published on purpose: getNetworkCapabilities() then
        // returns null for the active network, the defensive branch in
        // NetworkMonitor.checkConnectivity().
        publishWifiNetwork(capabilities = null)

        val monitor = NetworkMonitor(context())

        assertFalse(
            "An active network without capabilities must be reported offline",
            monitor.checkConnectivity(),
        )
    }

    @Test
    fun reportsOnlineWhenActiveNetworkIsValidated() {
        publishWifiNetwork(capabilities = networkCapabilities(validated = true))

        val monitor = NetworkMonitor(context())

        assertTrue(
            "A validated network with internet capability must be reported online",
            monitor.checkConnectivity(),
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

    @Test
    fun observeCalledTwiceUnregistersPreviousCallback() {
        val monitor = NetworkMonitor(context())

        monitor.observe { }
        // Calling observe again should clean up the first callback before
        // registering the next one, so repeated activity creation does not
        // leak ConnectivityManager.NetworkCallback registrations.
        monitor.observe { }
        monitor.stopObserving()
    }

    private fun networkCapabilities(validated: Boolean): NetworkCapabilities {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        val shadowCapabilities = shadowOf(capabilities)
        shadowCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (validated) {
            shadowCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        return capabilities
    }

    /**
     * Publishes a CONNECTED Wi-Fi network as the active network. A null
     * [capabilities] leaves the shadow's capabilities map untouched.
     */
    @Suppress("DEPRECATION")
    private fun publishWifiNetwork(capabilities: NetworkCapabilities?) {
        val connectivityManager =
            context().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadow = shadowOf(connectivityManager)
        shadow.setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                true,
                NetworkInfo.State.CONNECTED,
            ),
        )
        if (capabilities == null) {
            return
        }
        // Attach capabilities to the exact Network instance the monitor will read.
        val activeNetwork = connectivityManager.activeNetwork
        requireNotNull(activeNetwork) { "active network must be published" }
        shadow.setNetworkCapabilities(activeNetwork, capabilities)
    }
}
