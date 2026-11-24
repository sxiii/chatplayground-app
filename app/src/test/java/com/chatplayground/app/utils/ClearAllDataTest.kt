package com.chatplayground.app.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ClearAllDataTest {

    private lateinit var context: Context
    private lateinit var cacheManager: OfflineCacheManager

    @After
    fun tearDown() {
        // Clean up SharedPreferences between tests
        val prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    @Test
    fun clearAllDataClearsOfflineCacheAndWebViewCacheAndCookiesAndPrefs() {
        context = ApplicationProvider.getApplicationContext()
        cacheManager = OfflineCacheManager(context.cacheDir)
        val webView = WebView(context)

        // Arrange: Cache something, add a cookie, and put a pref
        val testUrl = "https://chatplayground.ai/cache-test"
        cacheManager.saveCachedResponse(testUrl, "cached-data")
        assertTrue("Cache should be populated", cacheManager.getCachedResponse(testUrl) != null)

        CookieManager.getInstance().setCookie(testUrl, "session=abc123")
        assertTrue("Cookies should exist", CookieManager.getInstance().hasCookies())

        val prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        prefs.edit().putString("test_key", "test_value").apply()

        // Act: Clear all data
        DataClearer.clearAllData(context, webView)

        // Assert: All data cleared
        assertEquals("Offline cache should be cleared", null, cacheManager.getCachedResponse(testUrl))
        assertTrue("WebView clearCache should be called", shadowOf(webView).wasClearCacheCalled())
        assertTrue("Cookies should be cleared", !CookieManager.getInstance().hasCookies())
        assertEquals("SharedPreferences should be cleared", null, prefs.getString("test_key", null))
    }

    @Test
    fun clearAllDataWithNoDataDoesNotThrow() {
        context = ApplicationProvider.getApplicationContext()
        cacheManager = OfflineCacheManager(context.cacheDir)
        val webView = WebView(context)

        // Ensure no data exists
        CookieManager.getInstance().removeAllCookies(null)
        val prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Should not throw
        DataClearer.clearAllData(context, webView)

        assertTrue(true)
    }
}
