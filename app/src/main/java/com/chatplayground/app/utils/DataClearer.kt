package com.chatplayground.app.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView

/**
 * Utility to clear all app data: cookies, WebView cache, and SharedPreferences.
 * Used by the menu "Clear All Data" action.
 */
object DataClearer {
    /** All SharedPreferences files the app uses, cleared by [clearAllData]. */
    private val PREF_FILES = listOf("app_data", "desktop_mode_prefs", "theme_prefs")

    /**
     * Clears all app data:
     * - Offline cache manager entries (stored as JSON files)
     * - WebView internal cache (cookies, disk cache, memory cache)
     * - All application SharedPreferences files (app_data, desktop_mode_prefs, theme_prefs)
     */
    fun clearAllData(
        context: Context,
        webView: WebView? = null,
    ) {
        // Clear offline cache
        OfflineCacheManager(context.cacheDir).clearCache()

        // Clear WebView cache and cookies
        webView?.clearCache(true)
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        // Clear all SharedPreferences files
        for (prefFile in PREF_FILES) {
            context.getSharedPreferences(prefFile, Context.MODE_PRIVATE).edit().clear().apply()
        }

        // Reset in-memory desktop-mode state so it matches the cleared storage
        DesktopModeHandler.applyDesktopMode(false)
    }
}
