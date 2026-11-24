package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkInfo

/**
 * Covers the defensive path in [NetworkMonitor.checkConnectivity] where the
 * platform reports an active network but no capabilities for it, e.g. the
 * default network dropped between the `activeNetwork` and
 * `getNetworkCapabilities` lookups.
 *
 * Robolectric 4.18 exposes no `ShadowConnectivityManager.setActiveNetwork(Network)`;
 * the supported way to publish an active network is
 * `ShadowConnectivityManager.setActiveNetworkInfo(NetworkInfo)`. Leaving the
 * capabilities map untouched makes `getNetworkCapabilities` return null.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkMonitorLoadErrorTest {
    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    @Suppress("DEPRECATION")
    fun reportsOfflineWhenNetworkCapabilitiesAreMissing() {
        val connectivityManager =
            context().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        shadowOf(connectivityManager).setActiveNetworkInfo(
            ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                true,
                NetworkInfo.State.CONNECTED,
            ),
        )

        // No setNetworkCapabilities call on purpose: the shadow returns null
        // from getNetworkCapabilities() for the active network.
        val monitor = NetworkMonitor(context())

        assertFalse(
            "A network without capabilities must be reported offline",
            monitor.checkConnectivity(),
        )
    }
}
