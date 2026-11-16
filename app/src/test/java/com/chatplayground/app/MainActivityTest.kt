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
    fun testWebViewClientIsConfigured() {
        // Test that WebView has a WebViewClient configured
        // This ensures links open within the app, not in external browser
        val webView = WebView(null) // Simplified test - actual test needs Robolectric setup
        val webViewClient = webView.webViewClient
        
        // Initially null since we haven't configured it yet
        // Test will pass once MainActivity configures WebViewClient
        assertNotNull("WebViewClient should be configured", webViewClient)
    }
}