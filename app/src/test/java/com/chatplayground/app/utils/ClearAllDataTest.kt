package com.chatplayground.app.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ClearAllDataTest {
    @Rule @JvmField
    val singletonCleanupRule = SingletonCleanupRule()

    private lateinit var context: Context
    private lateinit var cacheManager: OfflineCacheManager

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

    @Test
    fun clearAllDataClearsAllSharedPreferencesIncludingDesktopModeAndThemePrefs() {
        context = ApplicationProvider.getApplicationContext()
        cacheManager = OfflineCacheManager(context.cacheDir)
        val webView = WebView(context)

        // Arrange: populate all three SharedPreferences files
        val appDataPrefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        appDataPrefs.edit().putString("app_key", "app_value").apply()

        val desktopModePrefs = context.getSharedPreferences("desktop_mode_prefs", Context.MODE_PRIVATE)
        desktopModePrefs.edit().putBoolean("desktop_mode", true).apply()

        val themePrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        themePrefs.edit().putInt("theme_mode", 1).apply()

        // Act: Clear all data
        DataClearer.clearAllData(context, webView)

        // Assert: All three SharedPreferences files are cleared
        assertEquals("app_data should be cleared", null, appDataPrefs.getString("app_key", null))
        assertEquals("desktop_mode_prefs should be cleared", false, desktopModePrefs.getBoolean("desktop_mode", false))
        assertEquals("theme_prefs should be cleared", 0, themePrefs.getInt("theme_mode", 0))
    }

    @Test
    fun clearAllDataResetsDesktopModeHandlerState() {
        context = ApplicationProvider.getApplicationContext()
        val webView = WebView(context)

        // Arrange: set desktop mode in-memory state to true
        DesktopModeHandler.applyDesktopMode(true)
        assertTrue(DesktopModeHandler.isDesktop)

        // Act: Clear all data
        DataClearer.clearAllData(context, webView)

        // Assert: DesktopModeHandler in-memory state is reset to false
        assertFalse(DesktopModeHandler.isDesktop)
    }
}
