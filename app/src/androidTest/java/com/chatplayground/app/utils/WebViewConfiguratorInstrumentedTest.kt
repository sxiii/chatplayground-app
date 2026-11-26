package com.chatplayground.app.utils

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewConfiguratorInstrumentedTest {
    @Test
    fun configureAppliesAllSecurityAndUiSettingsToRealWebView() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val webView = WebView(context)

        WebViewConfigurator.configure(webView)

        val settings = webView.settings
        assertTrue("JavaScript should be enabled on a real WebView", settings.javaScriptEnabled)
        assertTrue("DOM storage should be enabled on a real WebView", settings.domStorageEnabled)
        assertEquals(
            "Mixed content mode should be NEVER_ALLOW on a real WebView",
            WebSettings.MIXED_CONTENT_NEVER_ALLOW,
            settings.mixedContentMode,
        )
        assertFalse("File access should be disabled on a real WebView", settings.allowFileAccess)
        assertFalse("Content access should be disabled on a real WebView", settings.allowContentAccess)
        assertEquals(
            "User-Agent should be ChatPlayground/1.0 on a real WebView",
            "ChatPlayground/1.0",
            settings.userAgentString,
        )
        assertFalse("Wide viewport should be off by default on a real WebView", settings.useWideViewPort)
        assertFalse("Load with overview mode should be off by default on a real WebView", settings.loadWithOverviewMode)
    }

    @Test
    fun configureIsIdempotentOnRealWebView() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val webView = WebView(context)

        WebViewConfigurator.configure(webView)
        WebViewConfigurator.configure(webView)

        val settings = webView.settings
        assertTrue("JavaScript should remain enabled after re-configure", settings.javaScriptEnabled)
        assertEquals(
            "User-Agent should remain ChatPlayground/1.0 after re-configure",
            "ChatPlayground/1.0",
            settings.userAgentString,
        )
        assertEquals(
            "Mixed content mode should remain NEVER_ALLOW after re-configure",
            WebSettings.MIXED_CONTENT_NEVER_ALLOW,
            settings.mixedContentMode,
        )
    }
}
