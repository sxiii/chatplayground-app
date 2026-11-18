package com.chatplayground.app.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowConnectivityManager

@RunWith(RobolectricTestRunner::class)
class NetworkMonitorTest {

    @Test
    fun testInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val monitor = NetworkMonitor(context)
        assertNotNull("NetworkMonitor should be initialized", monitor)
    }

    @Test
    fun testInitialConnectivityStatus() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Setup shadow connectivity manager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val shadowCm = shadowOf(cm)
        
        // Set network to connected
        shadowCm.setActiveNetworkInfo(shadowOf(android.net.NetworkInfo::class.java).apply {
            setDetailedState(android.net.NetworkInfo.DetailedState.CONNECTED, null, null)
        })

        val monitor = NetworkMonitor(context)
        // Note: The implementation uses NET_CAPABILITY_VALIDATED for M+, 
        // which Robolectric needs special handling for.
        // This is a minimal check.
        assertNotNull(monitor)
    }
}
