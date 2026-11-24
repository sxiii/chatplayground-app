package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkInfo

/**
 * Captive-portal validation coverage for [NetworkMonitor.checkConnectivity].
 *
 * Robolectric 4.18 shadows verified against the pinned tag:
 *   - ShadowConnectivityManager.setActiveNetworkInfo(NetworkInfo) publishes an
 *     active network and returns a ShadowNetwork.newInstance()-backed Network
 *     from getActiveNetwork().
 *   - ShadowConnectivityManager.setNetworkCapabilities(Network, NetworkCapabilities)
 *     stores capabilities keyed by network; read back via getNetworkCapabilities().
 *   - ShadowNetworkCapabilities.addCapability(int) adds a capability bit.
 *
 * checkConnectivity() requires both NET_CAPABILITY_INTERNET and
 * NET_CAPABILITY_VALIDATED on the active network, so a captive portal
 * (internet but not validated) must report offline.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorCaptivePortalTest {

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun reportsOfflineWhenNetworkHasInternetButNotValidated() {
        val connectivityManager =
            context().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadow = shadowOf(connectivityManager)

        // Publish an active Wi-Fi network.
        val network = ShadowNetwork.newInstance(1)
        shadow.setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                true,
                NetworkInfo.State.CONNECTED,
            ),
        )

        // Capabilities: INTERNET only — the captive portal has not validated.
        val caps = org.robolectric.shadows.ShadowNetworkCapabilities.newInstance()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadow.setNetworkCapabilities(network, caps)

        val monitor = NetworkMonitor(context())
        assertFalse(
            "Internet without validation (captive portal) must report offline",
            monitor.checkConnectivity(),
        )
    }

    @Test
    fun reportsOnlineWhenNetworkIsValidatedAndHasInternet() {
        val connectivityManager =
            context().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadow = shadowOf(connectivityManager)

        val network = ShadowNetwork.newInstance(2)
        shadow.setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                true,
                NetworkInfo.State.CONNECTED,
            ),
        )

        // Capabilities: both INTERNET and VALIDATED — fully online.
        val caps = org.robolectric.shadows.ShadowNetworkCapabilities.newInstance()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        shadow.setNetworkCapabilities(network, caps)

        val monitor = NetworkMonitor(context())
        assertTrue(
            "Validated network with internet must report online",
            monitor.checkConnectivity(),
        )
    }
}
