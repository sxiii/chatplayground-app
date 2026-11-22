package com.chatplayground.app

import android.view.View
import android.widget.ProgressBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Verifies that MainActivity displays a native loading indicator while the
 * WebView is performing a page load and hides it once the load completes.
 *
 * Runs on the suite-wide SDK from `app/src/test/resources/robolectric.properties`
 * (26): Robolectric 4.18 cannot load the WebView stack at API 34 and throws
 * NoClassDefFoundError there.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityLoadingIndicatorTest {
    private fun findLoadingProgress(activity: MainActivity): ProgressBar? {
        val id = activity.resources.getIdentifier("loading_progress", "id", activity.packageName)
        if (id == 0) return null
        return activity.findViewById<ProgressBar>(id)
    }

    @Test
    fun loadingIndicatorIsVisibleWhenPageLoadStarts() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val progress = findLoadingProgress(activity)

        assertNotNull(
            "MainActivity should contain a loading_progress view",
            progress,
        )
        assertEquals(
            "The loading indicator should be visible while the page is loading",
            View.VISIBLE,
            progress!!.visibility,
        )
    }

    @Test
    fun loadingIndicatorIsHiddenWhenPageLoadCompletes() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val progress = findLoadingProgress(activity)
        assertNotNull("MainActivity should contain a loading_progress view", progress)

        val webView = activity.getWebView()
        val chromeClient = webView.webChromeClient
        assertNotNull(
            "MainActivity should install a WebChromeClient to track load progress",
            chromeClient,
        )

        chromeClient!!.onProgressChanged(webView, 100)

        assertEquals(
            "The loading indicator should be hidden once the page finishes loading",
            View.GONE,
            progress!!.visibility,
        )
    }

    @Test
    fun loadingIndicatorRemainsVisibleDuringPartialLoad() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val progress = findLoadingProgress(activity)
        assertNotNull("MainActivity should contain a loading_progress view", progress)

        val webView = activity.getWebView()
        val chromeClient = webView.webChromeClient
        assertNotNull(
            "MainActivity should install a WebChromeClient to track load progress",
            chromeClient,
        )

        chromeClient!!.onProgressChanged(webView, 50)

        assertEquals(
            "The loading indicator should remain visible while the load is still in progress",
            View.VISIBLE,
            progress!!.visibility,
        )
    }

    @Test
    fun loadingIndicatorIsShownOnPageStarted() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val progress = findLoadingProgress(activity)
        assertNotNull(progress)
        progress!!.visibility = View.GONE

        val webView = activity.getWebView()
        val client = webView.webViewClient
        assertNotNull(client)

        client!!.onPageStarted(webView, "https://chatplayground.ai", null)
        assertEquals(View.VISIBLE, progress.visibility)
    }

    @Test
    fun loadingIndicatorIsHiddenOnPageFinished() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val progress = findLoadingProgress(activity)
        assertNotNull(progress)
        progress!!.visibility = View.VISIBLE

        val webView = activity.getWebView()
        val client = webView.webViewClient
        assertNotNull(client)

        client!!.onPageFinished(webView, "https://chatplayground.ai")
        assertEquals(View.GONE, progress.visibility)
    }

    @Test
    fun loadingIndicatorIsHiddenOnReceivedError() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val progress = findLoadingProgress(activity)
        assertNotNull(progress)
        progress!!.visibility = View.VISIBLE

        val webView = activity.getWebView()
        val client = webView.webViewClient
        assertNotNull(client)

        client!!.onReceivedError(webView, android.webkit.WebViewClient.ERROR_HOST_LOOKUP, "Error", "https://chatplayground.ai")
        assertEquals(View.GONE, progress.visibility)
    }
}
