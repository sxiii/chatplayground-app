package com.chatplayground.app.utils

import android.content.Context
import android.webkit.WebView
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class CacheClearerTest {

    @Test
    fun clearAllClearsOfflineCacheAndWebViewCache() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheManager = OfflineCacheManager(context.cacheDir)
        val url = "https://chatplayground.ai/cache-test"

        cacheManager.saveCachedResponse(url, "cached-data")
        assertTrue(cacheManager.getCachedResponse(url) != null)

        val webView = WebView(context)
        CacheClearer.clearAll(context, webView)

        assertFalse(cacheManager.getCachedResponse(url) != null)
        assertTrue(shadowOf(webView).wasClearCacheCalled())
    }

    @Test
    fun clearAllWithNoCachedFilesDoesNotThrow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheManager = OfflineCacheManager(context.cacheDir)
        cacheManager.clearCache()

        val webView = WebView(context)
        CacheClearer.clearAll(context, webView)
    }
}
