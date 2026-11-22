package com.chatplayground.app

import android.webkit.WebViewClient
import android.widget.Toast
import org.junit.Assert.assertEquals
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
        assertNull("No toast should be shown for a sub-resource error", ShadowToast.getLatestToast())
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
        assertEquals("Should show a toast on main-frame network error", Toast.LENGTH_SHORT, latestToast!!.duration)
        assertEquals("Network error occurred. Showing offline page.", ShadowToast.getTextOfLatestToast())
    }
}
