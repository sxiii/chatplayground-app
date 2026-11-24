package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

/**
 * Capability-change and unavailability coverage for [NetworkMonitor.observe].
 *
 * Robolectric does not replay real ConnectivityManager transitions, so the
 * tests drive the registered `NetworkCallback` directly:
 *   - `ShadowConnectivityManager.setActiveNetworkInfo(NetworkInfo)` publishes
 *     the active network; `setNetworkCapabilities(Network, NetworkCapabilities)`
 *     keys capabilities by that exact Network instance.
 *   - `NetworkCapabilities.addCapability(int)` is a hidden framework API and is
 *     reached through `Shadows.shadowOf(capabilities)`.
 *
 * Two behaviours are pinned here:
 *   1. A capability change without a network loss (captive portal: VALIDATED
 *      disappears while the Wi-Fi link stays up) must not leave `isConnected`
 *      stale.
 *   2. `onUnavailable()` must report offline and notify the observer.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorCapabilityChangeTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cm: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Before
    fun clearRegisteredCallbacks() {
        shadowOf(cm).networkCallbacks.clear()
    }

    private fun registeredCallback(): ConnectivityManager.NetworkCallback =
        shadowOf(cm).networkCallbacks.single()

    @Test
    fun onCapabilitiesChangedReportsOfflineWhenValidationIsLost() {
        // The monitor is created before the network is published so its initial
        // state is "offline" and the later transitions are observable.
        val monitor = NetworkMonitor(context)
        assertFalse("no network yet, so the monitor starts offline", monitor.isConnected)

        publishWifiNetwork(validated = true)
        val events = mutableListOf<Boolean>()
        monitor.observe { events.add(it) }

        val callback = registeredCallback()
        val network = requireNotNull(cm.activeNetwork) { "active network must be published" }
        callback.onAvailable(network)
        assertTrue("validated network must start online", monitor.isConnected)
        assertEquals("observer must hear the initial online transition", listOf(true), events)

        // The Wi-Fi link stays up but the portal drops validation: the platform
        // delivers only onCapabilitiesChanged(), never onLost().
        val unvalidated = publishWifiNetwork(validated = false)
        callback.onCapabilitiesChanged(network, unvalidated)

        assertFalse(
            "isConnected must follow capability changes, not only onAvailable/onLost",
            monitor.isConnected,
        )
        assertEquals(
            "observer must be told the device went offline after validation was lost",
            false,
            events.last(),
        )

        monitor.stopObserving()
    }

    @Test
    fun unchangedCapabilitiesDoNotNotifyAgain() {
        val monitor = NetworkMonitor(context)
        publishWifiNetwork(validated = true)
        val events = mutableListOf<Boolean>()
        monitor.observe { events.add(it) }

        val callback = registeredCallback()
        val network = requireNotNull(cm.activeNetwork) { "active network must be published" }
        callback.onAvailable(network)

        // A capability refresh that leaves the online state untouched must not
        // re-notify the consumer: MainActivity reloads the WebView from it.
        val stillValidated = publishWifiNetwork(validated = true)
        callback.onCapabilitiesChanged(network, stillValidated)

        assertTrue("still-validated network must stay online", monitor.isConnected)
        assertEquals("unchanged state must not be re-published", listOf(true), events)

        monitor.stopObserving()
    }

    @Test
    fun onUnavailableReportsOfflineAndNotifiesObserver() {
        val monitor = NetworkMonitor(context)
        publishWifiNetwork(validated = true)
        val events = mutableListOf<Boolean>()
        monitor.observe { events.add(it) }

        val callback = registeredCallback()
        callback.onAvailable(requireNotNull(cm.activeNetwork) { "active network must be published" })
        assertTrue(monitor.isConnected)

        // registerDefaultNetworkCallback() could not satisfy the request.
        callback.onUnavailable()

        assertFalse(
            "onUnavailable must set isConnected = false",
            monitor.isConnected,
        )
        assertEquals(
            "onUnavailable must notify the observer that the device is offline",
            false,
            events.last(),
        )

        monitor.stopObserving()
    }

    @Suppress("DEPRECATION")
    private fun publishWifiNetwork(validated: Boolean): NetworkCapabilities {
        val shadow = shadowOf(cm)
        shadow.setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                true,
                NetworkInfo.State.CONNECTED,
            ),
        )

        val capabilities = ShadowNetworkCapabilities.newInstance()
        val shadowCapabilities = shadowOf(capabilities)
        shadowCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (validated) {
            shadowCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        val activeNetwork = requireNotNull(cm.activeNetwork) { "active network must be published" }
        shadow.setNetworkCapabilities(activeNetwork, capabilities)
        return capabilities
    }
}