package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.os.HandlerThread
import android.os.Looper
import android.webkit.WebView
import java.util.concurrent.CountDownLatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork

/**
 * Behavioral test for [MainActivity]'s `NetworkMonitor.observe` callback.
 *
 * The `ConnectivityManager.NetworkCallback` fires on the connectivity agent
 * thread, so the whole decision (reading `webView.url` and choosing whether to
 * reload) must be marshalled onto the UI thread by `runOnUiThread`. This test
 * drives `onAvailable` from a background thread while the WebView sits on the
 * offline page and asserts that `WebView.reload()` runs on the main thread.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityNetworkMonitorTest {

    @Test
    fun networkMonitorRunsOfflinePageReloadOnMainThread() {
        // Build the activity: onCreate registers the NetworkMonitor callback.
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        // Precondition: WebView is showing the offline fallback page.
        val reloadedOn = mutableListOf<String>()
        val recorder = object : WebView(activity) {
            override fun reload() {
                reloadedOn.add(Thread.currentThread().name)
            }
        }
        activity.webView = recorder
        shadowOf(recorder).loadUrl("file:///android_asset/offline.html")
        assertEquals(
            "WebView must start on the offline fallback page",
            "file:///android_asset/offline.html",
            recorder.url
        )

        // Locate the NetworkCallback the activity registered (it fires on the
        // connectivity agent thread, not the UI thread).
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callbacks = shadowOf(cm).networkCallbacks.toList()
        assertTrue("NetworkMonitor must have registered a callback", callbacks.isNotEmpty())

        // Drive onAvailable from a background thread, exactly as the real
        // ConnectivityManager dispatches it.
        val mainThreadName = Looper.getMainLooper().thread.name
        val latch = CountDownLatch(1)
        Thread("ConnectivityAgent").apply {
            start()
            val network = ShadowNetwork.newInstance(1)
            callbacks.forEach { it.onAvailable(network) }
            latch.countDown()
        }
        latch.await()

        // Flush the main looper so runOnUiThread actions execute.
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue("reload() must have been invoked", reloadedOn.isNotEmpty())
        assertTrue(
            "reload() must run on the UI thread ($mainThreadName), not the connectivity agent thread",
            reloadedOn.any { it == mainThreadName }
        )
    }
}
