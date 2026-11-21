package com.chatplayground.app

import android.webkit.WebViewClient
import android.widget.Toast
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class MainActivityLoadErrorNotificationTest {

    @Test
    fun activityShowsToastOnNetworkError() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val webViewClient = webView.webViewClient

        // Simulate a network error
        webViewClient?.onReceivedError(
            webView,
            WebViewClient.ERROR_HOST_LOOKUP,
            "Failed to resolve host",
            "https://chatplayground.ai"
        )

        val latestToast = ShadowToast.getLatestToast()
        assertEquals("Should show a toast on network error", Toast.LENGTH_SHORT, latestToast.duration)
        assertEquals("Network error occurred. Showing offline page.", ShadowToast.getTextOfLatestToast())
    }
}
