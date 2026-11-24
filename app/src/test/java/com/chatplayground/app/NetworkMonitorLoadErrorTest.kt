package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowConnectivityManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class NetworkMonitorLoadErrorTest {
    private fun context(): Context = ApplicationProvider.getApplicationContext()
    
    @Test
    fun reportsOfflineWhenNetworkCapabilitiesAreMissing() {
        val monitor = NetworkMonitor(context())
        val connectivityManager = context().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadowConnectivityManager = shadowOf(connectivityManager)

        // Set to offline
        shadowConnectivityManager.setActiveNetwork(null)

        assertFalse(
            "Monitor should report offline when no active network",
            monitor.checkConnectivity(),
        )
    }
}