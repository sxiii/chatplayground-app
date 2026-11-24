package com.chatplayground.app.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView

/**
 * Utility to clear all app data: cookies, WebView cache, and SharedPreferences.
 * Used by the menu "Clear All Data" action.
 */
object DataClearer {
    /**
     * Clears all app data:
     * - Offline cache manager entries (stored as JSON files)
     * - WebView internal cache (cookies, disk cache, memory cache)
     * - Application SharedPreferences
     */
    fun clearAllData(
        context: Context,
        webView: WebView,
    ) {
        // Clear offline cache
        OfflineCacheManager(context.cacheDir).clearCache()

        // Clear WebView cache and cookies
        webView.clearCache(true)
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        // Clear all SharedPreferences
        val prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
