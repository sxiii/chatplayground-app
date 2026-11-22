package com.chatplayground.app

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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

    private class TestWebResourceRequest(
        private val url: String,
        private val forMainFrame: Boolean
    ) : WebResourceRequest {
        override fun getUrl(): android.net.Uri = android.net.Uri.parse(url)
        override fun isForMainFrame(): Boolean = forMainFrame
        override fun isRedirect(): Boolean = false
        override fun hasGesture(): Boolean = false
        override fun getMethod(): String = "GET"
        override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
    }

    private class TestWebResourceError(private val code: Int, private val desc: String?) : WebResourceError() {
        override fun getErrorCode(): Int = code
        override fun getDescription(): String? = desc
    }

    @Test
    fun subResourceErrorLeavesPageLoadedAndShowsNoToast() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val client = webView.webViewClient
        assertNotNull("WebViewClient should be set", client)

        // Precondition: the main page is loaded.
        assertEquals("https://chatplayground.ai", shadowOf(webView).lastLoadedUrl)

        // A sub-resource (e.g. an image) fails to load.
        client!!.onReceivedError(
            webView,
            TestWebResourceRequest("https://chatplayground.ai/assets/logo.png", forMainFrame = false),
            TestWebResourceError(WebViewClient.ERROR_HOST_LOOKUP, "Failed to resolve host")
        )

        // The current page must remain loaded.
        assertEquals(
            "A sub-resource error must not replace the current page",
            "https://chatplayground.ai",
            shadowOf(webView).lastLoadedUrl
        )
        // No toast must be shown for sub-resource failures.
        assertNull("No toast should be shown for a sub-resource error", ShadowToast.getLatestToast())
    }

    @Test
    fun mainFrameErrorStillFallsBackToOfflinePage() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val client = webView.webViewClient
        assertNotNull("WebViewClient should be set", client)

        // A main-frame navigation fails.
        client!!.onReceivedError(
            webView,
            TestWebResourceRequest("https://chatplayground.ai/", forMainFrame = true),
            TestWebResourceError(WebViewClient.ERROR_HOST_LOOKUP, "Failed to resolve host")
        )

        assertEquals(
            "A main-frame network error should fall back to the offline page",
            "file:///android_asset/offline.html",
            shadowOf(webView).lastLoadedUrl
        )
        val latestToast = ShadowToast.getLatestToast()
        assertEquals("Should show a toast on main-frame network error", Toast.LENGTH_SHORT, latestToast!!.duration)
        assertEquals("Network error occurred. Showing offline page.", ShadowToast.getTextOfLatestToast())
    }
}
