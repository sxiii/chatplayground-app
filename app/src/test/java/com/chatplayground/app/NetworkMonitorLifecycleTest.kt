package com.chatplayground.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkMonitorLifecycleTest {
    @Test
    fun testStopObservingDoesNotThrow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val monitor = NetworkMonitor(context)
        // No exception should be thrown even if no observer registered.
        monitor.stopObserving()
    }
}
