package com.chatplayground.app.utils

import android.content.Context
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Instrumentation-level coverage for [DesktopModeHandler]: verifies the
 * viewport + user-agent toggle actually takes effect on a real [WebView]
 * widget (Robolectric's [WebView] shadow is partial, so this runs on the
 * androidTest source set rather than plain JVM unit tests).
 */
class DesktopModeHandlerInstrumentedTest {

    @Test
    fun applyDesktopModeAppliesDesktopUaAndViewportToRealWebView() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val webView = WebView(context)

        DesktopModeHandler.applyDesktopMode(true)
        DesktopModeHandler.applyTo(webView)

        assertTrue("Desktop mode must set a desktop UA on a real WebView",
            webView.settings.userAgentString.contains("Chrome") && webView.settings.userAgentString.contains("Windows"))
        assertTrue("Desktop mode must enable wide viewport on a real WebView", webView.settings.useWideViewPort)
        assertTrue("Desktop mode must load with overview mode on a real WebView", webView.settings.loadWithOverviewMode)
    }

    @Test
    fun applyDesktopModeAppliesMobileUaAndViewportToRealWebView() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val webView = WebView(context)

        DesktopModeHandler.applyDesktopMode(false)
        DesktopModeHandler.applyTo(webView)

        assertEquals("Mobile mode must set mobile UA on a real WebView", "ChatPlayground/1.0", webView.settings.userAgentString)
        assertFalse("Mobile mode must not use wide viewport on a real WebView", webView.settings.useWideViewPort)
        assertFalse("Mobile mode must not load with overview on a real WebView", webView.settings.loadWithOverviewMode)
    }
}
