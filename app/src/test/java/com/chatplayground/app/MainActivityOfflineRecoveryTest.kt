package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.os.Looper
import android.webkit.WebView
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork
import java.util.concurrent.CountDownLatch

@RunWith(RobolectricTestRunner::class)
class MainActivityOfflineRecoveryTest {
    @Test
    fun offlineRecoveryLoadsOriginalUrlInsteadOfOfflinePage() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        // Precondition: WebView loaded a real URL, then failed and loaded offline page.
        val targetUrl = "https://chatplayground.ai/app"
        val recorder =
            object : WebView(activity) {
                var lastLoadedUrl: String? = null

                override fun loadUrl(url: String) {
                    lastLoadedUrl = url
                    super.loadUrl(url)
                }
            }
        activity.webView = recorder

        activity.lastTargetUrl = targetUrl
        // Simulate original load
        recorder.loadUrl(targetUrl)

        // Simulate failure load
        recorder.loadUrl("file:///android_asset/offline.html")
        assertEquals("file:///android_asset/offline.html", recorder.url)

        // Locate NetworkCallback
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callbacks = shadowOf(cm).networkCallbacks.toList()

        // Drive onAvailable
        val latch = CountDownLatch(1)
        Thread("ConnectivityAgent").apply {
            start()
            val network = ShadowNetwork.newInstance(1)
            callbacks.forEach { it.onAvailable(network) }
            latch.countDown()
        }
        latch.await()
        shadowOf(Looper.getMainLooper()).idle()

        // Verify it reloads the targetUrl, not offline.html
        assertEquals("Should reload the target URL", targetUrl, recorder.lastLoadedUrl)
    }

    @Test
    fun handleWebViewErrorLoadsOfflinePageOnHostLookup() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val recorder =
            object : WebView(activity) {
                var lastLoadedUrl: String? = null

                override fun loadUrl(url: String) {
                    lastLoadedUrl = url
                    super.loadUrl(url)
                }
            }
        activity.webView = recorder

        activity.handleWebViewError(recorder, android.webkit.WebViewClient.ERROR_HOST_LOOKUP, true)
        assertEquals("file:///android_asset/offline.html", recorder.lastLoadedUrl)
    }
}
