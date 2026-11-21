package com.chatplayground.app

import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import com.chatplayground.app.utils.UrlProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class MainActivityTest {

    @Test
    fun urlProviderReturnsChatPlaygroundUrl() {
        assertEquals("https://chatplayground.ai", UrlProvider.getChatPlaygroundUrl())
    }

    @Test
    fun activityConfiguresWebViewClient() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        assertNotNull("WebView should be created in onCreate", webView)
        assertNotNull("WebViewClient should be set", webView.webViewClient)
        assertTrue("WebViewClient should be set", webView.webViewClient != null)
    }

    @Test
    fun activityLoadsChatPlaygroundUrl() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        assertEquals(
            "MainActivity should load the ChatPlayground URL into its WebView",
            "https://chatplayground.ai",
            shadowOf(activity.getWebView()).lastLoadedUrl
        )
    }

    @Test
    fun activityLoadsOfflinePageOnNetworkError() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val webViewClient = webView.webViewClient

        webViewClient?.onReceivedError(
            webView,
            android.webkit.WebViewClient.ERROR_HOST_LOOKUP,
            "Failed to resolve host",
            "https://chatplayground.ai"
        )

        assertEquals(
            "MainActivity should load the offline fallback page on network error",
            "file:///android_asset/offline.html",
            shadowOf(webView).lastLoadedUrl
        )
    }

    @Test
    fun activityUpdatesTitleWhenWebViewTitleChanges() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val chromeClient = webView.webChromeClient

        val newTitle = "ChatPlayground - Home"
        chromeClient?.onReceivedTitle(webView, newTitle)

        assertEquals("ChatPlayground - Home", activity.title)
    }

    private fun requestFor(url: String): WebResourceRequest {
        return object : WebResourceRequest {
            override fun getUrl(): android.net.Uri = android.net.Uri.parse(url)
            override fun isForMainFrame(): Boolean = true
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
        }
    }

    @Test
    fun webViewInterceptsNonHttpSchemes() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val handled = webView.webViewClient?.shouldOverrideUrlLoading(
            webView,
            requestFor("mailto:support@chatplayground.ai")
        )

        assertTrue("WebViewClient should intercept non-HTTP schemes like mailto:", handled ?: false)
    }

    @Test
    fun webViewAllowsHttpHttpsSchemes() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val handled = webView.webViewClient?.shouldOverrideUrlLoading(
            webView,
            requestFor("https://chatplayground.ai/about")
        )

        assertFalse("WebViewClient should NOT intercept HTTP/HTTPS schemes", handled ?: false)
    }

    @Test
    fun webViewSavesAndRestoresScrollPosition() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).create().start().resume()
        val activity = controller.get()
        val webView = activity.getWebView()
        val shadowWebView = shadowOf(webView)

        shadowWebView.scrollTo(100, 200)

        val bundle = android.os.Bundle()
        controller.saveInstanceState(bundle).pause().stop().destroy()

        val newController = Robolectric.buildActivity(MainActivity::class.java).create(bundle).start().restoreInstanceState(bundle).resume()
        val newActivity = newController.get()
        val newWebView = newActivity.getWebView()

        assertEquals("WebView should restore scrollX position", 100, newWebView.scrollX)
        assertEquals("WebView should restore scrollY position", 200, newWebView.scrollY)
    }
}
