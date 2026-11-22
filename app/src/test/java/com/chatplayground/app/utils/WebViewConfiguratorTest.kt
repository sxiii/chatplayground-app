package com.chatplayground.app.utils

import android.webkit.WebSettings
import android.webkit.WebView
import com.chatplayground.app.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Pins the app-wide WebView configuration. `chatplayground.ai` is a
 * client-rendered single-page app, so the stock WebView defaults (JavaScript
 * and DOM storage both disabled) cannot render it at all.
 */
@RunWith(RobolectricTestRunner::class)
class WebViewConfiguratorTest {
    private fun configuredWebView(): WebView {
        val webView = WebView(RuntimeEnvironment.getApplication())
        WebViewConfigurator.configure(webView)
        return webView
    }

    @Test
    fun configureEnablesJavaScript() {
        assertTrue(
            "The chatplayground.ai SPA cannot render without JavaScript",
            configuredWebView().settings.javaScriptEnabled,
        )
    }

    @Test
    fun configureEnablesDomStorage() {
        assertTrue(
            "Session state lives in localStorage/sessionStorage",
            configuredWebView().settings.domStorageEnabled,
        )
    }

    @Test
    fun configureNeverAllowsMixedContent() {
        assertEquals(
            "HTTPS pages must not load HTTP sub-resources",
            WebSettings.MIXED_CONTENT_NEVER_ALLOW,
            configuredWebView().settings.mixedContentMode,
        )
    }

    @Test
    fun configureDisablesLocalFileAndContentAccess() {
        val settings = configuredWebView().settings
        assertFalse("The wrapper never renders local files", settings.allowFileAccess)
        assertFalse("The wrapper never renders content:// URIs", settings.allowContentAccess)
    }

    @Test
    fun configureIsIdempotent() {
        val webView = WebView(RuntimeEnvironment.getApplication())
        WebViewConfigurator.configure(webView)
        WebViewConfigurator.configure(webView)

        assertTrue(webView.settings.javaScriptEnabled)
        assertEquals(WebSettings.MIXED_CONTENT_NEVER_ALLOW, webView.settings.mixedContentMode)
    }

    @Test
    fun activityAppliesWebViewConfigurationOnCreate() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val settings = activity.getWebView().settings

        assertTrue(
            "MainActivity must configure the WebView before loading",
            settings.javaScriptEnabled,
        )
        assertTrue(
            "MainActivity must configure DOM storage before loading",
            settings.domStorageEnabled,
        )
        assertEquals(
            "MainActivity must apply the mixed-content policy",
            WebSettings.MIXED_CONTENT_NEVER_ALLOW,
            settings.mixedContentMode,
        )
    }
}
