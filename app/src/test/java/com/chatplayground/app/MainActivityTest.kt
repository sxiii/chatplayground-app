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
        assertEquals("https://chatplayground.com", UrlProvider.getChatPlaygroundUrl())
    }

    @Test
    fun activityConfiguresWebViewClient() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        assertNotNull("WebView should be created in onCreate", webView)
        assertNotNull("WebViewClient should be set", webView.webViewClient)
        assertTrue(
            "WebViewClient should be a platform WebViewClient",
            webView.webViewClient is WebViewClient
        )
    }

    @Test
    fun activityLoadsChatPlaygroundUrl() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        assertEquals(
            "MainActivity should load the ChatPlayground URL into its WebView",
            "https://chatplayground.com",
            shadowOf(activity.getWebView()).lastLoadedUrl
        )
    }
}
