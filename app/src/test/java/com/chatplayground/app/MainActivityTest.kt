package com.chatplayground.app

import android.webkit.WebViewClient
import com.chatplayground.app.utils.UrlProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class MainActivityTest {

    @Test
    fun urlProviderReturnsChatPlaygroundUrl() {
        assertEquals("https://chatplayground.ai", UrlProvider.getChatPlaygroundUrl())
    }

    @Test
    fun activityConfiguresWebViewClient() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        assertNotNull("WebView should be created in onCreate", webView)
        assertNotNull("WebViewClient should be set", webView.webViewClient)
        assertTrue(
            "WebViewClient should be set",
            webView.webViewClient != null
        )
    }

    @Test
    fun activityLoadsChatPlaygroundUrl() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        assertEquals(
            "MainActivity should load the ChatPlayground URL into its WebView",
            "https://chatplayground.ai",
            shadowOf(activity.getWebView()).lastLoadedUrl
        )
    }

    @Test
    fun activityLoadsOfflinePageOnNetworkError() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val webViewClient = webView.webViewClient

        // Simulate a network error (e.g., host lookup failed)
        webViewClient?.onReceivedError(
            webView,
            android.webkit.WebViewClient.ERROR_HOST_LOOKUP,
            "Failed to resolve host",
            "https://chatplayground.ai"
        )

        assertEquals(
            "MainActivity should load the offline fallback page on network error",
            "file:///android_asset/offline.html",
            shadowOf(webView).lastLoadedUrl
        )
    }
}
