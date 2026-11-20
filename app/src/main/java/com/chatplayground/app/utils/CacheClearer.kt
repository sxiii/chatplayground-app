package com.chatplayground.app.utils

import android.content.Context
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider

/**
 * Utility to clear both the offline cache and WebView's internal cache.
 * Used by the menu "Clear Cache" action.
 */
object CacheClearer {

    /**
     * Clears all cached data:
     * - Offline cache manager entries (stored as JSON files)
     * - WebView internal cache (cookies, disk cache, memory cache)
     */
    fun clearAll(context: Context, webView: WebView) {
        // Clear offline cache
        OfflineCacheManager(context.cacheDir).clearCache()

        // Clear WebView cache
        webView.clearCache(true)
        webView.clearCookies()
    }
}