package com.chatplayground.app

import android.webkit.WebView
import android.webkit.WebViewClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class MainActivityTest {

    @Test
    fun testUrlProviderUrl() {
        // Test that UrlProvider returns expected URL
        val expectedUrl = "https://chatplayground.com"
        val urlProvider = com.chatplayground.app.utils.UrlProvider
        val actualUrl = urlProvider.getChatPlaygroundUrl()
        
        assertEquals("UrlProvider should return the ChatPlayground URL", expectedUrl, actualUrl)
    }

    @Test
    fun testWebViewIsInitialized() {
        // Instantiate activity via Robolectric
        val activity = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).create().get()
        assertNotNull("WebView should be initialized", activity.webView)
    }
}
