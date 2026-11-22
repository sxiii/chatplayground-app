package com.chatplayground.app

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Verifies that MainActivity routes deep links into the WebView instead of
 * always starting on the default URL.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityDeepLinkTest {
    private fun viewIntent(uri: String): Intent {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setClass(context, MainActivity::class.java)
        }
    }

    @Test
    fun launchIntentDeepLinkIsLoadedInsteadOfDefaultUrl() {
        val activity =
            Robolectric.buildActivity(
                MainActivity::class.java,
                viewIntent("https://chatplayground.ai/chat/42"),
            ).create().get()

        assertEquals(
            "A deep link must replace the default start URL",
            "https://chatplayground.ai/chat/42",
            shadowOf(activity.getWebView()).lastLoadedUrl,
        )
    }

    @Test
    fun launchIntentCustomSchemeIsLoadedAsHttpsUrl() {
        val activity =
            Robolectric.buildActivity(
                MainActivity::class.java,
                viewIntent("chatplayground://app/chat/42"),
            ).create().get()

        assertEquals(
            "chatplayground://... must be normalised to the https URL",
            "https://chatplayground.ai/chat/42",
            shadowOf(activity.getWebView()).lastLoadedUrl,
        )
    }

    @Test
    fun launchIntentWithUnsupportedUrlKeepsDefaultUrl() {
        val activity =
            Robolectric.buildActivity(
                MainActivity::class.java,
                viewIntent("https://example.com/phish"),
            ).create().get()

        assertEquals(
            "Unsupported links must not hijack the start URL",
            "https://chatplayground.ai",
            shadowOf(activity.getWebView()).lastLoadedUrl,
        )
    }

    @Test
    fun newIntentWhileRunningNavigatesExistingWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        activity.onNewIntent(viewIntent("https://chatplayground.ai/settings"))

        assertEquals(
            "onNewIntent must navigate the existing WebView",
            "https://chatplayground.ai/settings",
            shadowOf(webView).lastLoadedUrl,
        )
    }

    @Test
    fun newIntentWithUnsupportedUrlLeavesWebViewAlone() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        activity.onNewIntent(viewIntent("https://example.com/phish"))

        assertEquals(
            "Unsupported onNewIntent data must not navigate",
            "https://chatplayground.ai",
            shadowOf(webView).lastLoadedUrl,
        )
    }
}
