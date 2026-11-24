package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

/**
 * Captive-portal validation coverage for [NetworkMonitor.checkConnectivity].
 *
 * Robolectric 4.18 APIs verified against the pinned tag's sources:
 *   - `ShadowConnectivityManager.setActiveNetworkInfo(NetworkInfo)` publishes an
 *     active network; `getActiveNetwork()` returns the Network registered for
 *     that NetworkInfo's type.
 *   - `ShadowConnectivityManager.setNetworkCapabilities(Network, NetworkCapabilities)`
 *     stores capabilities keyed by the exact Network instance.
 *   - `NetworkCapabilities.addCapability(int)` is a hidden framework API, so the
 *     test calls it through `Shadows.shadowOf(capabilities)`.
 *
 * checkConnectivity() requires both NET_CAPABILITY_INTERNET and
 * NET_CAPABILITY_VALIDATED on the active network, so a captive portal
 * (internet but not validated) must report offline.
 *
 * The positive counterpart (INTERNET + VALIDATED reports online) lives in
 * `NetworkMonitorTest.reportsOnlineWhenActiveNetworkIsValidated` so the whole
 * capability truth table stays in one place.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorCaptivePortalTest {
    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Suppress("DEPRECATION")
    private fun publishWifiNetwork(connectivityManager: ConnectivityManager) {
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

        val capabilities = ShadowNetworkCapabilities.newInstance()
        val shadowCapabilities = shadowOf(capabilities)
        shadowCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        // Attach capabilities to the exact Network instance the monitor will read.
        val activeNetwork = connectivityManager.activeNetwork
        requireNotNull(activeNetwork) { "active network must be published" }
        shadow.setNetworkCapabilities(activeNetwork, capabilities)
    }

    @Test
    fun reportsOfflineWhenNetworkHasInternetButNotValidated() {
        val connectivityManager =
            context().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        publishWifiNetwork(connectivityManager)

        val monitor = NetworkMonitor(context())
        assertFalse(
            "Internet without validation (captive portal) must report offline",
            monitor.checkConnectivity(),
        )
    }
}
