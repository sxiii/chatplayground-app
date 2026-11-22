package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork

/**
 * Behavioral tests for [NetworkMonitor.observe].
 *
 * Robolectric does not replay real ConnectivityManager transitions on its own,
 * but [org.robolectric.shadows.ShadowConnectivityManager] exposes the set of
 * registered callbacks. The tests therefore drive `onAvailable`/`onLost`
 * explicitly instead of relying on the default-network shadow to fire them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class NetworkMonitorObserveTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cm: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun registeredCallbacks() = shadowOf(cm).networkCallbacks

    @Before
    fun clearRegisteredCallbacks() {
        // ShadowConnectivityManager keeps registrations in a static set; clear it
        // so each test starts from a known state.
        registeredCallbacks().clear()
    }

    @Test
    fun observeReportsOnlineThenOfflineOnNetworkTransitions() {
        val monitor = NetworkMonitor(context)
        val events = mutableListOf<Boolean>()

        monitor.observe { events.add(it) }

        val callbacks = registeredCallbacks().toList()
        assertEquals("observe() must register exactly one callback", 1, callbacks.size)

        val network = ShadowNetwork.newInstance(1)
        callbacks.forEach { it.onAvailable(network) }

        assertTrue("monitor should be online once onAvailable fires", monitor.isConnected)
        assertEquals("observer should be told the device is online", listOf(true), events)

        callbacks.forEach { it.onLost(network) }

        assertFalse("monitor should be offline once onLost fires", monitor.isConnected)
        assertEquals("observer should be told the device went offline", listOf(true, false), events)

        monitor.stopObserving()
    }

    @Test
    fun observeCalledTwiceReplacesPreviousCallback() {
        val monitor = NetworkMonitor(context)

        monitor.observe { }
        assertEquals(1, registeredCallbacks().size)

        monitor.observe { }
        // The previous callback must be unregistered before the new one is
        // registered, otherwise activity recreation leaks registrations.
        assertEquals(
            "repeated observe() must not leak ConnectivityManager callbacks",
            1,
            registeredCallbacks().size
        )

        monitor.stopObserving()
        assertTrue(
            "stopObserving() must unregister the callback",
            registeredCallbacks().isEmpty()
        )
    }

    @Test
    fun stopObservingWithoutObserveIsNoOp() {
        val monitor = NetworkMonitor(context)

        monitor.stopObserving()
        monitor.stopObserving()

        assertTrue(registeredCallbacks().isEmpty())
    }
}