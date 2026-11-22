package com.chatplayground.app

import android.webkit.WebViewClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.webResourceError
import android.widget.Toast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast

/**
 * Verifies that a failed sub-resource (an image, analytics beacon, iframe, …)
 * does not replace the whole page with the offline fallback, while a main-frame
 * network error still falls back to `offline.html` with a toast.
 *
 * Runs on the suite-wide SDK from `app/src/test/resources/robolectric.properties`
 * (26).
 */
@RunWith(RobolectricTestRunner::class)
class MainActivitySubResourceErrorTest {
    @Test
    fun subResourceErrorLeavesPageLoadedAndShowsNoToast() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        // Precondition: the main page is loaded.
        assertEquals("https://chatplayground.ai", shadowOf(webView).lastLoadedUrl)

        // A sub-resource (e.g. an image) fails to load — not main frame, so nothing happens.
        activity.handleWebViewError(webView, WebViewClient.ERROR_HOST_LOOKUP, isMainFrame = false)

        // The current page must remain loaded.
        assertEquals(
            "A sub-resource error must not replace the current page",
            "https://chatplayground.ai",
            shadowOf(webView).lastLoadedUrl,
        )
        // No toast must be shown for sub-resource failures.
        assertNull(
            "No toast should be shown for a sub-resource error",
            ShadowToast.getLatestToast(),
        )
    }

    @Test
    fun mainFrameErrorStillFallsBackToOfflinePage() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        // A main-frame navigation fails.
        activity.handleWebViewError(webView, WebViewClient.ERROR_HOST_LOOKUP, isMainFrame = true)

        assertEquals(
            "A main-frame network error should fall back to the offline page",
            "file:///android_asset/offline.html",
            shadowOf(webView).lastLoadedUrl,
        )
        val latestToast = ShadowToast.getLatestToast()
        assertEquals(
            "Should show a toast on main-frame network error",
            Toast.LENGTH_SHORT,
            latestToast!!.duration,
        )
        assertEquals(
            "Network error occurred. Showing offline page.",
            ShadowToast.getTextOfLatestToast(),
        )
    }

    @Test
    fun webViewClientOnReceivedErrorMainFrameFallsBackToOfflinePage() {
        ShadowToast.reset()
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val client = webView.webViewClient ?: return

        val error = webResourceError(WebViewClient.ERROR_HOST_LOOKUP, "Failed to resolve host")
        client.onReceivedError(
            webView,
            resourceRequestFor("https://chatplayground.ai", isMainFrame = true),
            error,
        )

        assertEquals(
            "A main-frame network error via WebResourceRequest falls back to the offline page",
            "file:///android_asset/offline.html",
            shadowOf(webView).lastLoadedUrl,
        )
        assertNotNull("A toast should be shown on main-frame network error", ShadowToast.getLatestToast())
    }

    @Test
    fun webViewClientOnReceivedErrorSubResourceLeavesPageLoaded() {
        ShadowToast.reset()
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val client = webView.webViewClient ?: return

        val error = webResourceError(WebViewClient.ERROR_HOST_LOOKUP, "Failed to resolve host")
        client.onReceivedError(
            webView,
            resourceRequestFor("https://chatplayground.ai/image.png", isMainFrame = false),
            error,
        )

        assertEquals(
            "A sub-resource error via WebResourceRequest leaves the current page loaded",
            "https://chatplayground.ai",
            shadowOf(webView).lastLoadedUrl,
        )
        assertNull("No toast should be shown for a sub-resource error via WebResourceRequest", ShadowToast.getLatestToast())
    }

    private fun resourceRequestFor(
        url: String,
        isMainFrame: Boolean,
    ): WebResourceRequest {
        return object : WebResourceRequest {
            override fun getUrl(): android.net.Uri = android.net.Uri.parse(url)
            override fun isForMainFrame(): Boolean = isMainFrame
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
        }
    }
}
