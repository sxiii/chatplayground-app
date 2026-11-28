package com.chatplayground.app.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.chatplayground.app.security.AppLock

/**
 * Utility to clear all app data: cookies, WebView cache, WebStorage, and
 * SharedPreferences.
 * Used by the menu "Clear All Data" action.
 */
object DataClearer {
    /** All SharedPreferences files the app uses, cleared by [clearAllData]. */
    private val PREF_FILES = listOf("app_data", "desktop_mode_prefs", "theme_prefs")

    /**
     * Seam for the [WebStorage] clearing call so [clearAllData] can be
     * unit-tested under Robolectric, whose [ShadowWebStorage] returns a stub
     * with no observable state. Defaults to the production call; tests swap in
     * a counter lambda to verify it was invoked.
     */
    @Volatile
    var webStorageDeleter: () -> Unit = { WebStorage.getInstance().deleteAllData() }

    /** Optional test observer for verifying destructive-operation ordering. */
    @Volatile
    var clearingStepObserver: ((String) -> Unit)? = null

    /** Restores the production [webStorageDeleter] after a test injected its own. */
    fun resetWebStorageDeleterForTesting() {
        webStorageDeleter = { WebStorage.getInstance().deleteAllData() }
    }

    /** Restores the absent production clearing-order observer. */
    fun resetClearingStepObserverForTesting() {
        clearingStepObserver = null
    }

    private fun observeClearingStep(step: String) {
        clearingStepObserver?.invoke(step)
    }

    /**
     * Clears all app data:
     * - Offline cache manager entries (stored as JSON files)
     * - WebView internal cache (cookies, disk cache, memory cache)
     * - WebStorage (localStorage / sessionStorage)
     * - All application SharedPreferences files (app_data, desktop_mode_prefs, theme_prefs)
     */
    fun clearAllData(
        context: Context,
        webView: WebView? = null,
    ) {
        // Clear offline cache
        observeClearingStep("offline_cache")
        OfflineCacheManager(context.cacheDir).clearCache()

        // Clear WebView cache and cookies
        observeClearingStep("webview_cache")
        webView?.clearCache(true)
        observeClearingStep("cookies")
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        // Clear WebStorage (localStorage / sessionStorage)
        observeClearingStep("web_storage")
        webStorageDeleter()

        // Clear all SharedPreferences files
        observeClearingStep("preferences")
        for (prefFile in PREF_FILES) {
            context.getSharedPreferences(prefFile, Context.MODE_PRIVATE).edit().clear().apply()
        }

        // Reset in-memory desktop-mode state so it matches the cleared storage
        observeClearingStep("desktop_mode")
        DesktopModeHandler.applyDesktopMode(false)

        // Reset AppLock so "Clear All Data" does not leave the app locked
        observeClearingStep("app_lock")
        AppLock.enabled = false
    }
}
