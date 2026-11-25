package com.chatplayground.app.utils

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebViewConfiguratorIntegrationTest {

    @Test
    fun configureAppliesAllSecurityAndUiSettings() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val webView = WebView(context)

        WebViewConfigurator.configure(webView)

        val settings = webView.settings
        assertTrue("JavaScript should be enabled", settings.javaScriptEnabled)
        assertTrue("DOM storage should be enabled", settings.domStorageEnabled)
        assertEquals("Mixed content mode should be NEVER_ALLOW", WebSettings.MIXED_CONTENT_NEVER_ALLOW, settings.mixedContentMode)
        assertFalse("File access should be disabled", settings.allowFileAccess)
        assertFalse("Content access should be disabled", settings.allowContentAccess)
        assertEquals("User-Agent should be ChatPlayground/1.0", "ChatPlayground/1.0", settings.userAgentString)
    }
}
