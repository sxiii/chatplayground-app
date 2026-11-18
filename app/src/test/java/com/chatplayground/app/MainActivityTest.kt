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
    fun testWebViewIsInitialized() {
        // Test that MainActivity creates and configures a WebView
        // This test will fail until we implement MainActivity with WebView
        val expectedUrl = "https://chatplayground.com"
        
        // Verify UrlProvider returns the expected URL
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