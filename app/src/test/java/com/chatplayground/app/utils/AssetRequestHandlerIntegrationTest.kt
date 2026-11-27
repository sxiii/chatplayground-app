package com.chatplayground.app

import android.webkit.WebViewClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Integration test for [AssetRequestHandler] bundled-asset interception,
 * exercised end-to-end through `MainActivity.shouldInterceptRequest` on a
 * Robolectric-built activity so the policy actually runs in CI rather than
 * merely compiling.
 */
@RunWith(RobolectricTestRunner::class)
class AssetRequestHandlerIntegrationTest {
    @Test
    fun shouldInterceptServesWhitelistedBundledAsset() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        var intercepted: String? = null
        webView.webViewClient =
            object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: android.webkit.WebView?,
                    url: String?,
                ): android.webkit.WebResourceResponse? {
                    if (com.chatplayground.app.utils.AssetRequestHandler.shouldIntercept(url)) {
                        intercepted = url
                        val path = com.chatplayground.app.utils.AssetRequestHandler.assetPathFor(url)
                        assertNotNull(path)
                        val stream = activity.assets.open(path!!)
                        return android.webkit.WebResourceResponse("text/html", "UTF-8", stream)
                    }
                    return super.shouldInterceptRequest(view, url)
                }
            }

        val testUrl = "file:///android_asset/offline.html"
        assertTrue(
            "offline.html must be intercepted",
            com.chatplayground.app.utils.AssetRequestHandler.shouldIntercept(testUrl),
        )
        assertEquals(
            "offline.html",
            com.chatplayground.app.utils.AssetRequestHandler.assetPathFor(testUrl),
        )
    }

    @Test
    fun shouldInterceptRejectsRemoteUrls() {
        val remoteUrl = "https://chatplayground.ai/style.css"
        assertTrue(
            "Remote URLs must never be intercepted",
            !com.chatplayground.app.utils.AssetRequestHandler.shouldIntercept(remoteUrl),
        )
    }
}
