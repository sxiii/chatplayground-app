package com.chatplayground.app

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityTest {
    @Test
    fun testWebViewClientIsConfigured() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        assertNotNull("WebViewClient should be set", webView.webViewClient)
    }
}
